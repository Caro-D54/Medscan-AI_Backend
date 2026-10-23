# ⚙️ MedScan AI - API REST (Backend)

Núcleo lógico y motor de datos de **MedScan AI**. API REST desarrollada con **Spring Boot** que gestiona usuarios, medicamentos, tratamientos y dosis sobre **PostgreSQL**, escanea recetas/prospectos con **OpenAI** y envía recordatorios push mediante **Expo**.

## 🚀 Características Técnicas

* **Arquitectura por capas:** `controller`, `service`, `repository`, `model`, más paquetes de soporte (`config`, `security`, `notification`, `scheduler`).
* **Persistencia:** Spring Data JPA sobre PostgreSQL. El esquema se versiona con **Flyway** (`src/main/resources/db/migration`).
* **Seguridad:** autenticación stateless con **JWT** (Spring Security) y CORS configurable.
* **Escaneo con IA:** `POST /api/v1/scan/process` extrae datos de medicamentos con la API de OpenAI.
* **Recordatorios push:** un scheduler revisa periódicamente las dosis próximas y notifica vía Expo Push. **ShedLock** evita ejecuciones duplicadas con varias instancias.
* **Operación (12-Factor):** configuración por variables de entorno, apagado controlado y endpoints de Actuator.
* **Contenedores y CI/CD:** Docker multi-stage, Docker Compose y pipeline en GitHub Actions con despliegue a Azure.

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| --- | --- |
| **Lenguaje** | Java 25 |
| **Framework** | Spring Boot 3.x |
| **Base de Datos** | PostgreSQL 18 |
| **Migraciones** | Flyway |
| **Autenticación** | JWT (jjwt) |
| **Concurrencia de tareas** | ShedLock (JDBC) |
| **Build** | Maven (Maven Wrapper incluido) |
| **Contenedores** | Docker / Docker Compose |

## ⚙️ Configuración e Instalación

### Requisitos

* JDK 25 (o Docker, para ejecución en contenedores).
* PostgreSQL 18 con una base de datos `medscan_db` (si no usas Docker Compose).

### Opción A: Ejecución local con Maven

1. Crea la base de datos `medscan_db`.
2. Define las variables de entorno (ver [Variables de Entorno](#-variables-de-entorno)) o copia `src/main/resources/application-example.yaml` a `src/main/resources/application-dev.yaml` y ajusta los valores locales.
3. Ejecuta:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   > El perfil por defecto es `dev` (`SPRING_PROFILES_ACTIVE`). El script `boot.ps1` lanza la aplicación con variables de entorno locales; revisa sus parámetros por defecto (credenciales, puerto, `ddl-auto`) antes de usarlo y no subas contraseñas reales.

### Opción B: Docker Compose (API + PostgreSQL)

1. Copia `.env.example` como `.env` y completa los valores.
2. Levanta los servicios:

   ```bash
   docker compose up --build
   ```

La API queda disponible en `http://localhost:8080` (puerto configurable con `SERVER_PORT`). PostgreSQL se publica en `POSTGRES_PORT` (por defecto `5432`). El `Dockerfile` define las etapas `builder`, `development` (con puerto de depuración 5005) y `production`.

## 🌐 Variables de Entorno

La API se configura mediante variables de entorno referenciadas desde `application.yaml`. Usa `.env.example` (Docker Compose) o `application-example.yaml` (ejecución local) como punto de partida. Los archivos `.env` y `application-dev.yaml` pueden contener credenciales: están ignorados en `.gitignore` y **no deben** subirse al repositorio.

| Variable | Descripción | Ejemplo / Valor por defecto |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`dev` o `prod`) | `dev` (local), `prod` (Docker) |
| `SERVER_PORT` | Puerto HTTP | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/medscan_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `change_me` |
| `POSTGRES_DB` / `POSTGRES_PORT` | Nombre y puerto publicado de PostgreSQL (Docker Compose) | `medscan_db` / `5432` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | `*` |
| `JWT_SECRET` | Clave para firmar los JWT (mínimo 32 caracteres) | `clave_secreta_de_al_menos_32_caracteres` |
| `JWT_EXPIRATION_MS` | Expiración del token en ms | `86400000` (24 h) |
| `SCAN_OPENAI_API_KEY` | API key de OpenAI para el escaneo | `sk-...` |
| `SCAN_OPENAI_MODEL` | Modelo de OpenAI (opcional) | `gpt-4o-mini` |
| `SCAN_OPENAI_URL` | Endpoint de Chat Completions (opcional) | `https://api.openai.com/v1/chat/completions` |
| `SCAN_OPENAI_TIMEOUT_MS` | Timeout de la llamada a OpenAI (opcional) | `15000` |
| `NOTIFICATION_EXPO_URL` | Endpoint de Expo Push (opcional) | `https://exp.host/--/api/v2/push/send` |
| `NOTIFICATION_EXPO_TIMEOUT_MS` | Timeout de Expo Push (opcional) | `10000` |
| `NOTIFICATION_REMINDER_DELAY_MS` | Intervalo entre revisiones de dosis pendientes (opcional) | `60000` |
| `NOTIFICATION_REMINDER_ADVANCE_MINUTES` | Minutos de antelación para avisar tomas (opcional) | `10` |

Las variables `AZURE_*` y `ACR_*` de `.env.example` solo se usan en el despliegue (ver [Despliegue](#-despliegue-y-cicd)).

## 📡 Endpoints Principales

Prefijo base: `/api/v1`. Salvo `auth` y los endpoints públicos listados abajo, todos requieren `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/auth/register` | Registro de usuario |
| `POST` | `/auth/login` | Inicio de sesión (devuelve JWT) |
| `GET` | `/users/me` | Datos del usuario autenticado |
| `PATCH` | `/users/me/push-token` | Registra el token de notificaciones push |
| `GET` / `POST` | `/medications` | Lista / crea medicamentos |
| `GET` / `PUT` / `DELETE` | `/medications/{id}` | Consulta, actualiza o elimina un medicamento |
| `POST` | `/scan/process` | Procesa una imagen y extrae el medicamento |
| `GET` / `POST` | `/treatments` | Lista / crea tratamientos |
| `PATCH` | `/treatments/doses/{id}/take` | Marca una dosis como tomada |

**Endpoints públicos:** `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health/**`, `/actuator/info` y `/actuator/metrics/**`.

La especificación completa del contrato (DTOs y códigos de error) está en `MedScan-AI_Frontend/docs/openapi.yaml`, que es la fuente de verdad compartida con el frontend.

## 🩺 Monitoreo

Actuator expone `health`, `info` y `metrics`. Los health probes están habilitados, por lo que puedes consultar `GET /actuator/health` (también usado por el healthcheck de Docker Compose).

## 🧪 Pruebas

```powershell
.\mvnw.cmd clean verify
```

En `postman/` hay una colección con entornos `Local` y `Azure` para probar la API manualmente.

## ☁️ Despliegue y CI/CD (Clean Pipelines)

La integración y el despliegue continuo se encuentran desacoplados en workflows independientes según el principio de responsabilidad única (Clean Architecture / Separation of Concerns):

1. **Integración Continua (`.github/workflows/ci.yml`)**:
   - Se ejecuta en `push` a `main`/`develop` y en `pull_request` a `main`.
   - **Build & Tests**: Compila y ejecuta todos los tests unitarios y de integración con `./mvnw -B clean verify` sobre Java 25 y contenedor PostgreSQL.
   - **Snyk (SCA & SAST)**: Analiza vulnerabilidades en dependencias Maven.
   - **Grype**: Escanea vulnerabilidades en capas base de contenedores Docker (`anchore/scan-action`).
   - *Nota de seguridad*: No posee acceso a credenciales de despliegue ni a infraestructura de Azure.

2. **Despliegue Continuo (`.github/workflows/cd.yml`)**:
   - Se dispara únicamente tras la finalización exitosa del CI (`workflow_run` sobre `CI Pipeline - MedScan AI Backend`). Un fallo en CI bloquea por completo el CD.
   - **Versionado Semántico (`vX.Y.Z`)**: Calcula la versión según Conventional Commits (`feat` -> Y, `fix`/`refactor` -> Z, `doc`/`chore` -> omitir).
   - **Publicación en ACR**: Construye y sube la imagen de producción al Azure Container Registry mediante Service Principal (`upload_to_acr.py`).
   - **Aprovisionamiento en ACI**: Despliega o actualiza la instancia en Azure Container Instances validando pre-registro de `Microsoft.ContainerInstance`.
   - **Smoke Tests & 12-Factor App**: Valida telemetría, métricas y estado de salud en vivo (`/actuator/health` y `/actuator/metrics`).
   - *Nota de seguridad*: Todos los secretos de infraestructura residen exclusivamente en este workflow y se leen desde GitHub Secrets.

3. **Monitoreo Continuo y Salud del Servicio (`.github/workflows/monitor.yml`)**:
   - Se ejecuta de manera programada cada 30 minutos (`schedule` cron) y bajo demanda (`workflow_dispatch`).
   - **Health Check Activo**: Realiza peticiones a `/actuator/health` y falla estrictamente si el código HTTP no es `200` o la aplicación no reporta estado `UP`.
   - **Telemetría y Probes (12-Factor App)**: Consulta `/actuator/health/liveness`, `/actuator/health/readiness` y métricas clave (`process.uptime`).
   - **Notificación Inteligente de Incidentes**: En caso de caída, crea o actualiza automáticamente una issue de alerta en GitHub (`🚨 [ALERTA] Fallo de Disponibilidad...`) con etiquetas y detalles de diagnóstico. Si el servicio se recupera, la alerta se resuelve y auto-cierra.
   - *Nota de seguridad*: Enmascara dinámicamente (`::add-mask::`) las URLs y tokens para evitar exponer endpoints o datos sensibles en los logs de ejecución.

Para subir la imagen a Azure Container Registry manualmente usa `upload_to_acr.py` (lee las variables de `.env`). Los comandos de referencia de Azure CLI están en [`docs/azure_comands.md`](docs/azure_comands.md).

## 📚 Documentación Adicional

* [`docs/azure_comands.md`](docs/azure_comands.md): comandos de Azure CLI (login, ACR, service principal).
* [`docs/diagrama_User.puml`](docs/diagrama_User.puml): diagrama del modelo de usuario.
* [`docs/medscan_use_cases.puml`](docs/medscan_use_cases.puml): casos de uso.