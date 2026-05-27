# Pharmacs_Backend

---

# 💊 MedScan AI: Gestión Inteligente de Medicación

**MedScan AI** es una solución integral diseñada para mejorar la adherencia al tratamiento médico y prevenir interacciones medicamentosas peligrosas. Mediante el uso de visión artificial y análisis de datos médicos, la aplicación permite a los usuarios gestionar su salud de forma segura y organizada.

---

## 🚀 Características Principales

* **Reconocimiento Visual:** Análisis de medicamentos a partir de una fotografía (OCR y Clasificación).
* **Información Detallada:** Consulta automática de principios activos, efectos secundarios y posología.
* **Interacciones Medicamentosas:** Cruce de datos para detectar si el medicamento es compatible con otros que el usuario ya esté tomando.
* **Guía Alimentaria:** Indicaciones sobre si la toma debe realizarse en ayunas, con alimentos o lejos de las comidas.
* **Planificador Inteligente:** Generación automática de horarios optimizados para evitar conflictos entre fármacos.
* **Sistema de Alertas:** Notificaciones push para asegurar que no se pierda ninguna toma.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| --- | --- |
| **Backend** | Spring Boot 3.x (Java 17+) |
| **Base de Datos** | PostgreSQL |
| **Seguridad** | Spring Security & JWT |
| **Documentación API** | Swagger / OpenAPI |
| **Gestión de Dependencias** | Maven |

---

## 🏗️ Arquitectura y Entidades

El backend está diseñado siguiendo una arquitectura limpia (Clean Architecture), asegurando que la lógica de negocio sea independiente de las herramientas externas.

### Modelo de Datos (PostgreSQL)

Las tablas principales incluyen:

* `Users`: Perfiles de usuario y preferencias.
* `Medicines`: Catálogo de drogas, efectos e interacciones.
* `User_Treatments`: Relación entre el usuario y su medicación actual.
* `Schedules`: Registro de horas exactas y estado de la toma (completada/pendiente).

---

## 🔌 Endpoints Principales (Resumen)

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/v1/scan` | Procesa la imagen y devuelve la info del medicamento. |
| `GET` | `/api/v1/interactions/{id}` | Comprueba interacciones con el historial del usuario. |
| `POST` | `/api/v1/schedule/generate` | Crea el calendario de tomas basado en la receta. |
| `GET` | `/api/v1/notifications` | Lista de alertas próximas. |

---

## ⚙️ Configuración del Entorno

1. **Clonar el repositorio:**
```bash
git clone https://github.com/tu-usuario/medscan-ai.git

```


2. **Configurar la base de datos:**
Crea una base de datos en PostgreSQL y actualiza el archivo `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/medscan_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

```


3. **Ejecutar la aplicación:**
```bash
./mvnw spring-boot:run

```



---

## 🛡️ Descargo de Responsabilidad (Disclaimer)

> [!IMPORTANT]
> Esta aplicación es una herramienta de apoyo y **no sustituye el consejo de un profesional médico**. Los datos de interacciones se basan en bases de datos integradas, pero siempre se debe consultar con un farmacéutico o doctor antes de iniciar un tratamiento.

---

## 👨‍💻 Autor

Desarrollado con ❤️ por un Ingeniero en Informática enfocado en soluciones de salud digital.



