## Login

```bash
az login --service-principal -u "$AZURE_APP_ID" -p "$AZURE_PASSWORD" --tenant "$AZURE_TENANT"
az acr login --name "$AZURE_ACR_NAME"
```

## Listar

```bash
az acr list
az acr repository list --name "$AZURE_ACR_NAME"
```

## Build y push de la imagen

```bash
docker build -t app-azure:<tag> .
docker tag app-azure:<tag> "$AZURE_ACR_NAME.azurecr.io/app-azure:<tag>"
docker push "$AZURE_ACR_NAME.azurecr.io/app-azure:<tag>"
```
## Verificar subida de imagen
az acr repository list --name ingenieriagrupo3

## Crear un service principal

```bash
az ad sp create-for-rbac --name grupo3 --role acrpush \
  --scopes /subscriptions/<subscription-id>/resourceGroups/<grupo-de-recursos>
```
## Actualizacion de service principal
az ad sp credential reset --id <app-id> --display-name grupo3-rotado