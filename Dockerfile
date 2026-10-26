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
# 2. DEVELOPMENT STAGE: Imagen para Desarrollo (JDK 25 completo + Hot-Reload / Debug)
#    Construcción: docker build --target development -t medscan-backend:dev .
# ------------------------------------------------------------------------------
FROM builder AS development

LABEL maintainer="MedScan AI DevOps Team" \
      description="MedScan AI Backend REST API - Development Container (JDK 25 + Debug)"

ENV SPRING_PROFILES_ACTIVE=dev \
    SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Exponer puerto HTTP (8080) y puerto de depuración remota JVM (5005)
EXPOSE 8080 5005

CMD ["./mvnw", "spring-boot:run"]

# ------------------------------------------------------------------------------
# 3. PRODUCTION STAGE (Default): Imagen para Producción con JRE 25 (Alpine) + Azure App Insights
#    Construcción: docker build --target production -t medscan-backend:prod .
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine AS production

LABEL maintainer="MedScan AI DevOps Team" \
      description="MedScan AI Backend REST API - Production Container with Azure Application Insights"

# Crear usuario y grupo de sistema sin privilegios para ejecución segura (Non-Root)
RUN addgroup -S -g 1001 medscan && adduser -S -u 1001 -G medscan medscan

WORKDIR /app

# Descargar Azure Application Insights Java Agent 3.x para telemetría, métricas y APM distribuido
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.2/applicationinsights-agent-3.6.2.jar /app/applicationinsights-agent.jar
RUN chown medscan:medscan /app/applicationinsights-agent.jar && chmod 644 /app/applicationinsights-agent.jar

# Optimización de JVM para contenedores:
# - UseContainerSupport: Detecta límites de CPU y Memoria asignados por Docker/K8s
# - MaxRAMPercentage=75: Asigna hasta el 75% de la memoria del contenedor a la JVM
# - java.security.egd: Acelera la generación de entropía segura para JWT
# - javaagent: Autoinstrumentación no intrusiva con Azure Application Insights
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -javaagent:/app/applicationinsights-agent.jar" \
    SPRING_PROFILES_ACTIVE=prod \
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
