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
| **Lenguaje** | Java 25+ |
| **Framework** | Spring Boot 3.x |
| **Base de Datos** | PostgreSQL 18+ |
| **Gestión de Dependencias** | Maven |

## ⚙️ Configuración e Instalación

1. **Requisitos:** Tener instalado el JDK 25 y PostgreSQL.
2. **Base de Datos:** Crea una base de datos llamada `medscan_db`.
3. **Configuración:** Define las variables de entorno (ver sección [Variables de Entorno](#-variables-de-entorno)) o copia la plantilla `src/main/resources/application-example.yaml` a `src/main/resources/application-dev.yaml` y ajusta los valores locales.
4. **Ejecución:**
   ```powershell
   .\mvnw.cmd spring-boot:run

## 🌐 Variables de Entorno

La API se configura mediante variables de entorno referenciadas desde `application.yaml`. Copia la plantilla `src/main/resources/application-example.yaml` como punto de partida y completa cada variable. La configuración local `application-dev.yaml`, que puede contener credenciales sensibles, está ignorada en `.gitignore` y **no debe** subirse al repositorio.

| Variable | Descripción | Ejemplo |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/medscan_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `change_me` |
| `JWT_SECRET` | Clave secreta para firmar los tokens JWT (mínimo 32 caracteres) | `clave_secreta_muy_larga_de_al_menos_32_caracteres` |
| `JWT_EXPIRATION_MS` | Tiempo de expiración del token JWT en milisegundos | `86400000` (24 h) |
| `SCAN_OPENAI_API_KEY` | API key de OpenAI para el servicio de escaneo de recetas/prospectos | `sk-...` |
| `SCAN_OPENAI_MODEL` | Modelo de OpenAI usado para el escaneo (opcional) | `gpt-4o-mini` |
| `SCAN_OPENAI_URL` | Endpoint de OpenAI Chat Completions (opcional) | `https://api.openai.com/v1/chat/completions` |