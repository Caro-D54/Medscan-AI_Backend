import argparse
import json
import os
import re
import subprocess
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")


# ============================================================
# CARGA AUTOMÁTICA DE .env (SIN DEPENDENCIAS EXTERNAS)
# ============================================================

def load_dotenv_file(dotenv_path: str = ".env") -> None:
    """
    Carga variables desde un archivo .env local si aún no están
    definidas en las variables de entorno del sistema.
    """
    if not os.path.exists(dotenv_path):
        return

    with open(dotenv_path, "r", encoding="utf-8") as f:
        for raw_line in f:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ and value:
                os.environ[key] = value


load_dotenv_file()

AZURE_APP_ID = os.getenv("AZURE_APP_ID")
AZURE_PASSWORD = os.getenv("AZURE_PASSWORD")
AZURE_ACR_NAME = os.getenv("AZURE_ACR_NAME")

# Normalizacion defensiva desde AZURE_CREDENTIALS si no existen variables individuales
AZURE_CREDENTIALS = os.getenv("AZURE_CREDENTIALS")
if AZURE_CREDENTIALS and (not AZURE_APP_ID or not AZURE_PASSWORD):
    try:
        creds_data = json.loads(AZURE_CREDENTIALS)
        AZURE_APP_ID = AZURE_APP_ID or creds_data.get("clientId") or creds_data.get("appId")
        AZURE_PASSWORD = AZURE_PASSWORD or creds_data.get("clientSecret") or creds_data.get("password")
        if not AZURE_ACR_NAME and creds_data.get("acrName"):
            AZURE_ACR_NAME = creds_data.get("acrName")
    except Exception:
        pass


# ============================================================
# DOCKER
# ============================================================

def is_docker_available() -> bool:
    """Verifica si Docker está disponible y si el motor responde."""

    try:
        result = subprocess.run(
            ["docker", "info"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )

        return result.returncode == 0

    except FileNotFoundError:
        return False


# ============================================================
# DOCKERFILE
# ============================================================

def extract_base_images_from_dockerfile(
    dockerfile_path: str = "Dockerfile",
) -> list[str]:
    """
    Lee el Dockerfile y extrae las imágenes base
    declaradas en las instrucciones FROM (ignorando alias internos).
    """

    if not os.path.exists(dockerfile_path):
        return []

    images = []
    stage_aliases = set()

    with open(dockerfile_path, "r", encoding="utf-8") as f:

        for line in f:

            line = line.strip()

            if not line or line.startswith("#"):
                continue

            match = re.match(
                r"^FROM\s+([^\s]+)(?:\s+AS\s+([^\s]+))?",
                line,
                re.IGNORECASE,
            )

            if match:

                image = match.group(1)
                alias = match.group(2)

                if (
                    image not in images
                    and image.lower() != "scratch"
                    and image.lower() not in stage_aliases
                ):
                    images.append(image)

                if alias:
                    stage_aliases.add(alias.lower())

    return images


# ============================================================
# LOGIN AZURE CONTAINER REGISTRY
# ============================================================

def login_to_azure_acr(
    acr_name: str,
    app_id: str,
    password: str,
) -> str | None:
    """
    Inicia sesión en Azure Container Registry
    utilizando una Service Principal.

    app_id   = App ID de la Service Principal
    password = Password de la Service Principal
    """

    clean_acr = acr_name.strip()

    if clean_acr.endswith(".azurecr.io"):
        registry_server = clean_acr
    else:
        registry_server = f"{clean_acr}.azurecr.io"

    print()
    print(
        f"🔑 Iniciando sesión en Azure Container Registry "
        f"({registry_server})..."
    )

    try:

        result = subprocess.run(
            [
                "docker",
                "login",
                registry_server,
                "--username",
                app_id,
                "--password-stdin",
            ],
            input=password,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )

        if result.returncode == 0:

            print(
                "[SUCCESS] Autenticación exitosa "
                "contra Azure Container Registry."
            )

            return registry_server

        else:

            print(
                "[ERROR] No se pudo autenticar "
                "contra Azure Container Registry."
            )

            print(result.stderr.strip())

            return None

    except Exception as e:

        print(
            f"[ERROR] Error durante la autenticación: {e}"
        )

        return None


# ============================================================
# PULL + TAG + PUSH (IMAGEN EXTERNA)
# ============================================================

def process_and_upload_image(
    image_name: str,
    registry_server: str,
) -> bool:
    """
    1. Descarga la imagen localmente.
    2. La etiqueta para Azure Container Registry.
    3. La sube mediante docker push.
    """

    print()
    print("==================================================")
    print(f"📦 Procesando imagen: {image_name}")
    print("==================================================")

    # --------------------------------------------------------
    # 1. PULL
    # --------------------------------------------------------

    print(
        f"\n[1/3] 📥 Descargando imagen "
        f"(`docker pull {image_name}`)..."
    )

    try:

        subprocess.run(
            ["docker", "pull", image_name],
            check=True,
        )

        print(
            f"[SUCCESS] Imagen '{image_name}' descargada."
        )

    except subprocess.CalledProcessError as e:

        print(
            f"[ERROR] Falló la descarga de "
            f"'{image_name}': {e}"
        )

        return False

    # --------------------------------------------------------
    # 2. TAG
    # --------------------------------------------------------

    clean_repo_name = image_name.split("/")[-1]

    target_azure_tag = (
        f"{registry_server}/{clean_repo_name}"
    )

    print(
        "\n[2/3] 🏷️ Etiquetando para Azure:"
    )

    print(
        f"      docker tag "
        f"{image_name} "
        f"{target_azure_tag}"
    )

    try:

        subprocess.run(
            [
                "docker",
                "tag",
                image_name,
                target_azure_tag,
            ],
            check=True,
        )

        print(
            f"[SUCCESS] Imagen etiquetada como "
            f"'{target_azure_tag}'."
        )

    except subprocess.CalledProcessError as e:

        print(
            f"[ERROR] Falló el etiquetado: {e}"
        )

        return False

    # --------------------------------------------------------
    # 3. PUSH
    # --------------------------------------------------------

    print(
        "\n[3/3] 🚀 Subiendo imagen a Azure:"
    )

    print(
        f"      docker push {target_azure_tag}"
    )

    try:

        subprocess.run(
            [
                "docker",
                "push",
                target_azure_tag,
            ],
            check=True,
        )

        print(
            "[SUCCESS] 🎉 ¡Imagen subida exitosamente "
            "a Azure!"
        )

        print(
            f"           {target_azure_tag}"
        )

        return True

    except subprocess.CalledProcessError as e:

        print(
            f"[ERROR] Falló la subida: {e}"
        )

        return False


# ============================================================
# BUILD + UPLOAD PROJECT IMAGE
# ============================================================

def build_and_upload_image(
    image_name: str,
    image_tag: str,
    dockerfile_path: str,
    registry_server: str | None,
    target_stage: str = "production",
    also_tag_latest: bool = False,
    build_only: bool = False,
) -> bool:
    """Construye la imagen de MedScan AI Backend, la etiqueta y la publica en ACR."""

    local_image = f"{image_name}:{image_tag}"
    build_context = os.path.dirname(os.path.abspath(dockerfile_path)) or "."

    base_images = extract_base_images_from_dockerfile(dockerfile_path)
    if base_images:
        print(f"🔎 Imágenes base detectadas en {dockerfile_path}: {', '.join(base_images)}")

    print()
    print("==================================================")
    print(f"🛠️ Construyendo imagen: {local_image} (target: {target_stage})")
    print("==================================================")

    try:
        build_cmd = [
            "docker",
            "build",
            "--file",
            dockerfile_path,
            "--target",
            target_stage,
            "--tag",
            local_image,
            build_context,
        ]
        subprocess.run(build_cmd, check=True)
        print(f"[SUCCESS] Imagen '{local_image}' (stage '{target_stage}') construida.")

        if build_only:
            print("[INFO] Modo --build-only activo: se omite el push a Azure ACR.")
            return True

        target_azure_tag = f"{registry_server}/{local_image}"

        subprocess.run(
            ["docker", "tag", local_image, target_azure_tag],
            check=True,
        )
        print(f"[SUCCESS] Imagen etiquetada como '{target_azure_tag}'.")

        subprocess.run(
            ["docker", "push", target_azure_tag],
            check=True,
        )
        print(f"[SUCCESS] Imagen publicada en Azure: {target_azure_tag}")

        if also_tag_latest and image_tag != "latest":
            latest_azure_tag = f"{registry_server}/{image_name}:latest"
            subprocess.run(
                ["docker", "tag", local_image, latest_azure_tag],
                check=True,
            )
            subprocess.run(
                ["docker", "push", latest_azure_tag],
                check=True,
            )
            print(f"[SUCCESS] Etiqueta adicional publicada en Azure: {latest_azure_tag}")

        return True

    except subprocess.CalledProcessError as error:
        print(f"[ERROR] No se pudo construir o publicar la imagen: {error}")
        return False


# ============================================================
# MAIN
# ============================================================

def main():

    parser = argparse.ArgumentParser(
        description=(
            "Construye la imagen de MedScan AI Backend y la publica "
            "automáticamente en Azure Container Registry (ACR)."
        )
    )

    parser.add_argument(
        "image",
        nargs="?",
        default=None,
        help=(
            "Imagen Docker externa a descargar y publicar. "
            "Si se omite, se construye la imagen del proyecto MedScan AI."
        ),
    )

    parser.add_argument(
        "--image-name",
        default="medscan-backend",
        help="Nombre de la imagen que se construirá (por defecto: medscan-backend).",
    )

    parser.add_argument(
        "--tag",
        default="latest",
        help="Tag de la imagen construida, ej. v1.1.0 o latest (por defecto: latest).",
    )

    parser.add_argument(
        "--target",
        choices=["production", "development"],
        default="production",
        help="Stage del Dockerfile multi-stage a construir: 'production' o 'development' (por defecto: production).",
    )

    parser.add_argument(
        "--also-tag-latest",
        action="store_true",
        help="Si se especifica un tag vX.Y.Z, también etiqueta y sube ':latest' en ACR.",
    )

    parser.add_argument(
        "--build-only",
        action="store_true",
        help="Solo construye y verifica la imagen localmente sin requerir credenciales ni subir a Azure.",
    )

    parser.add_argument(
        "--acr",
        default=None,
        help=(
            "Nombre o URL del Azure Container Registry "
            "(ej: ingenieria o ingenieria.azurecr.io)."
        ),
    )

    parser.add_argument(
        "--dockerfile",
        default="Dockerfile",
        help="Ruta al Dockerfile.",
    )

    args = parser.parse_args()

    print(
        "=== Script de Integración y Subida "
        "a Azure Container Registry (MedScan AI) ==="
    )

    # ========================================================
    # 1. COMPROBAR DOCKER
    # ========================================================

    if not is_docker_available():

        print(
            "[ERROR] Docker CLI no está disponible "
            "o Docker Desktop no se está ejecutando."
        )

        print(
            "        Asegúrate de iniciar Docker Desktop "
            "en tu sistema."
        )

        sys.exit(1)

    print(
        "[SUCCESS] Docker está disponible."
    )

    # Modo solo compilación local (para testear antes de subir a Azure)
    if args.build_only:
        success = build_and_upload_image(
            args.image_name,
            args.tag,
            args.dockerfile,
            registry_server=None,
            target_stage=args.target,
            build_only=True,
        )
        if not success:
            sys.exit(1)
        print("==================================================")
        print(f"🎉 ¡ÉXITO! Imagen local '{args.image_name}:{args.tag}' ({args.target}) validada.")
        print("==================================================")
        sys.exit(0)

    # ========================================================
    # 2. COMPROBAR CREDENCIALES DE SERVICE PRINCIPAL Y ACR
    # ========================================================

    acr_input = args.acr or AZURE_ACR_NAME

    if not AZURE_APP_ID:
        print("[ERROR] No configuraste AZURE_APP_ID (App ID del Service Principal).")
        sys.exit(1)

    if not AZURE_PASSWORD:
        print("[ERROR] No configuraste AZURE_PASSWORD (Password/Secret del Service Principal).")
        sys.exit(1)

    if not acr_input:
        print("[ERROR] No configuraste AZURE_ACR_NAME ni pasaste --acr.")
        sys.exit(1)

    # ========================================================
    # 3. LOGIN EN AZURE CONTAINER REGISTRY
    # ========================================================

    registry_server = login_to_azure_acr(
        acr_input,
        AZURE_APP_ID,
        AZURE_PASSWORD,
    )

    if not registry_server:
        print(
            "[ERROR] No se pudo autenticar "
            "en Azure Container Registry."
        )
        sys.exit(1)

    # ========================================================
    # 4. CONSTRUIR Y PUBLICAR LA IMAGEN DEL PROYECTO
    # ========================================================

    if args.image:
        success = process_and_upload_image(args.image, registry_server)
        processed_description = f"la imagen externa '{args.image}'"
    else:
        success = build_and_upload_image(
            args.image_name,
            args.tag,
            args.dockerfile,
            registry_server,
            target_stage=args.target,
            also_tag_latest=args.also_tag_latest,
        )
        processed_description = (
            f"la imagen '{args.image_name}:{args.tag}' ({args.target}) del proyecto"
        )

    # ========================================================
    # 5. RESULTADO FINAL
    # ========================================================

    print()
    print("==================================================")

    if success:
        print(
            f"🎉 ¡ÉXITO! Se publicó {processed_description} "
            f"en Azure Container Registry."
        )
        print(
            f"📦 Registry: {registry_server}"
        )
    else:
        print(
            f"⚠️ No se pudo publicar {processed_description}."
        )
        sys.exit(1)

    print("==================================================")


# ============================================================
# EJECUCIÓN
# ============================================================

if __name__ == "__main__":
    main()
