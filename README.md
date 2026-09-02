# ⚙️ MedScan AI - API REST (Backend)

Este repositorio contiene el núcleo lógico y el motor de datos de **MedScan AI**. Es una API REST robusta desarrollada con **Spring Boot** que gestiona la persistencia en **PostgreSQL** y el procesamiento de información médica.

## 🚀 Características Técnicas

* **Arquitectura:** Clean Architecture con separación de capas (`controller`, `service`, `repository`, `model`).
* **Persistencia:** Integración nativa con PostgreSQL mediante Spring Data JPA.
* **Seguridad:** Configuración de CORS para permitir integraciones de clientes externos.
* **Gestión de Entidades:** Automatización de esquema de base de datos a través de Hibernate.

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| --- | --- |
| **Lenguaje** | Java 21+ |
| **Framework** | Spring Boot 3.x |
| **Base de Datos** | PostgreSQL 18+ |
| **Gestión de Dependencias** | Maven |

## ⚙️ Configuración e Instalación

1. **Requisitos:** Tener instalado el JDK 21 y PostgreSQL.
2. **Base de Datos:** Crea una base de datos llamada `medscan_db`.

### Variables de entorno requeridas

| Variable | Descripción |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC a PostgreSQL (ej. `jdbc:postgresql://localhost:5432/medscan_db`) |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña del usuario de la base de datos |
| `JWT_SECRET` | Secreto usado para firmar los tokens JWT (mín. 32 caracteres) |
| `JWT_EXPIRATION_MS` | (Opcional) Tiempo de expiración del token en milisegundos. Default: `86400000` (24 h) |

### Pasos

1. **Configurar el entorno de desarrollo:** Copiá `application-example.yaml` a `application-dev.yaml` y completá tus credenciales locales (este archivo está ignorado por Git y nunca se sube).

   ```powershell
   Copy-Item src\main\resources\application-example.yaml src\main\resources\application-dev.yaml
   ```

   > El perfil activo por defecto es `dev`, por lo que Spring Boot carga `application-dev.yaml` automáticamente.

2. **Configurar variables de entorno (alternativa):** Si preferís no usar el archivo `application-dev.yaml`, seteá las variables del cuadro anterior (o creá un `.env` a partir de `.env.example`).

3. **Ejecución:**
   ```powershell
   .\mvnw.cmd spring-boot:run