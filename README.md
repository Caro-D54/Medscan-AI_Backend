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
3. **Configuración:** Actualiza el archivo `src/main/resources/application.yaml` (o `.properties`) con tus credenciales locales.
4. **Ejecución:**
   ```powershell
   .\mvnw.cmd spring-boot:run