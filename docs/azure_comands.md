# Guía de Comandos Azure & DevOps (MedScan AI Backend)

Este documento detalla la gestión de infraestructura en Azure (ACR, ACI y Service Principals) y la configuración de GitHub Secrets requeridos por los pipelines de CD (`cd.yml`) y Monitoreo (`monitor.yml`).

---

## 1. Autenticación Local con Azure CLI

```bash
# Iniciar sesión interactiva como usuario
az login

# Iniciar sesión con Service Principal
az login --service-principal -u "$AZURE_APP_ID" -p "$AZURE_PASSWORD" --tenant "$AZURE_TENANT"

# Autenticar Docker contra Azure Container Registry (ACR)
az acr login --name "$AZURE_ACR_NAME"
```

---

## 2. Gestión de Azure Container Registry (ACR)

```bash
# Listar registros de contenedores disponibles
az acr list -o table

# Listar repositorios en el ACR ingenieriagrupo3
az acr repository list --name ingenieriagrupo3 -o table

# Listar tags de la imagen del backend
az acr repository show-tags --name ingenieriagrupo3 --repository medscan-backend -o table
```

### Construcción y Subida de Imagen Docker

Para compilar y publicar imágenes automáticamente se recomienda usar el script oficial:

```bash
# Subir tag semántico vX.Y.Z y actualizar :latest en ACR
python upload_to_acr.py --image-name medscan-backend --tag v1.1.2 --target production --also-tag-latest
```

---

## 3. Service Principal y Permisos para CI/CD

El Service Principal `grupo3` requiere roles para:
1. `AcrPush` sobre el Container Registry (`ingenieriagrupo3`).
2. `Contributor` sobre el Resource Group (`universidad`) para poder crear/actualizar Azure Container Instances (ACI).

```bash
# Crear o regenerar Service Principal con formato compatible con GitHub Actions
az ad sp create-for-rbac \
  --name "grupo3" \
  --role "Contributor" \
  --scopes "/subscriptions/ea92660f-9a3d-4fb7-b490-7505c0113c56/resourceGroups/universidad" \
  --sdk-auth
```

> **Nota de Clean DevOps:** La bandera `--sdk-auth` genera un JSON con claves `clientId`, `clientSecret`, `subscriptionId` y `tenantId`, compatible de forma nativa con `azure/login@v2`. La pipeline `cd.yml` implementa además normalización automática para aceptar el formato clásico (`appId`, `password`, `tenant`).

```bash
# Resetear credenciales en caso de expiración o rotación:
az ad sp credential reset --id "7dbc689d-72c7-4fa8-8daf-8dd966ed2a0a" --display-name "grupo3-rotado"
```

---

## 4. Despliegue Manual en Azure Container Instances (ACI)

```bash
# Desplegar o actualizar el contenedor del backend
az container create \
  --resource-group "universidad" \
  --name "medscan-backend-aci" \
  --image "ingenieriagrupo3.azurecr.io/medscan-backend:latest" \
  --dns-name-label "medscan-api" \
  --ports 8080 \
  --cpu 1 \
  --memory 1.5 \
  --registry-login-server "ingenieriagrupo3.azurecr.io" \
  --registry-username "<ACR_USERNAME>" \
  --registry-password "<ACR_PASSWORD>" \
  --environment-variables \
      SPRING_PROFILES_ACTIVE=prod \
      SERVER_PORT=8080 \
      SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
      SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  --secure-environment-variables \
      SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD"

# Verificar estado y logs de la instancia desplegada
az container show --resource-group "universidad" --name "medscan-backend-aci" -o table
az container logs --resource-group "universidad" --name "medscan-backend-aci"
```

---

## 5. Monitoreo y Resolución Automática de Incidentes (Issue #24)

- **Workflow:** `.github/workflows/monitor.yml` (se ejecuta cada 30 min y vía `workflow_dispatch`).
- **Endpoint de Salud:** `http://medscan-api.eastus2.azurecontainer.io:8080/actuator/health`.
- **Comportamiento:**
  - Si el servicio no responde o el estado es diferente de `UP`, se genera/actualiza automáticamente una issue de incidente con etiqueta `Priority: High,Infraestructure` (ej. Issue #24).
  - En cuanto el despliegue se estabiliza y el health check responde `HTTP 200 OK` / `status: UP`, el workflow **auto-cierra la issue de incidente** con un comentario de resolución.
