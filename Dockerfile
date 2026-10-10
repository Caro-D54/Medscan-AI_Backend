# ==============================================================================
# MedScan AI Backend - Multi-Stage Dockerfile (Java 25 / Spring Boot 3)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. BUILD STAGE: Compilación del código con Maven Wrapper y OpenJDK 25
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

# Copiar configuración de dependencias y wrapper de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Asegurar terminaciones de línea Unix (LF) en mvnw y otorgar permisos de ejecución
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargar dependencias en capa separada para aprovechar la caché de Docker
RUN ./mvnw dependency:go-offline -B || ./mvnw dependency:resolve -B

# Copiar el código fuente de la aplicación
COPY src ./src

# Compilar y generar el paquete JAR ejecutable (omitiendo tests, ya validados en CI)
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# 2. RUNTIME STAGE: Entorno de ejecución ultra liviano con JRE 25 (Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine AS runtime

LABEL maintainer="MedScan AI DevOps Team" \
      description="MedScan AI Backend REST API - Production Container"

# Crear usuario y grupo de sistema sin privilegios para ejecución segura (Non-Root)
RUN addgroup -S -g 1001 medscan && adduser -S -u 1001 -G medscan medscan

WORKDIR /app

# Optimización de JVM para contenedores:
# - UseContainerSupport: Detecta límites de CPU y Memoria asignados por Docker/K8s
# - MaxRAMPercentage=75: Asigna hasta el 75% de la memoria del contenedor a la JVM
# - java.security.egd: Acelera la generación de entropía segura para JWT
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom" \
    SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

# Copiar el artefacto generado desde la etapa builder asignando permisos al usuario sin privilegios
COPY --from=builder --chown=medscan:medscan /workspace/target/*.jar /app/app.jar

# Cambiar al usuario no-root
USER medscan:medscan

# Exponer el puerto del servidor Spring Boot
EXPOSE 8080

# Healthcheck nativo para verificar disponibilidad mediante Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 'exec' reemplaza el proceso de shell por java, permitiendo recibir señales SIGTERM para apagado controlado (Graceful Shutdown)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
