# SGA - Sistema de Gestión de Alojamiento [Búnker Orbital YoRHa - Suites & Apartamentos Temáticos]

Este proyecto es una aplicación desarrollada en Java con Spring Boot para la gestión y administración de alojamientos.

## Integrantes del Equipo
* Tomás Castaño Ortiz
* Camilo Ruiz López

## Requisitos
* **Java Development Kit (JDK):** Versión 25
* **Gradle Wrapper:** Incluido en el proyecto (no requiere instalación previa de Gradle)

## Instrucciones de Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Camilo2B/sga_YorHaSA.git
   cd sga
   ```

2. **Ejecutar las pruebas:**
   ```bash
   ./gradlew test
   ```
   El reporte queda en `build/reports/tests/test/index.html`.

3. **Arrancar la aplicación:**
   ```bash
   ./gradlew bootRun
   ```
   La API queda en `http://localhost:8080`. Al arrancar se cargan solos los datos de prueba: el alojamiento con sus
   parámetros, temporadas y política de cancelación, los 15 apartamentos de la Ficha con sus tarifas, un bloqueo y tres
   reservas (pendiente, confirmada y cancelada).

## Documentación y pruebas de la API
* **Contrato:** `docs/api/openapi.yaml`.
* **Swagger UI:** `http://localhost:8080/swagger-ui.html` (muestra el mismo `openapi.yaml`).
* **Colección de pruebas:** `docs/api/apartamentos.http` (cliente HTTP de IntelliJ). Se ejecuta completa, en orden,
  contra la aplicación recién arrancada.
* **Consola de H2:** `http://localhost:8080/h2-console` con la URL JDBC `jdbc:h2:mem:sgadb` y el usuario `sa`.
* **Modelo de dominio y matriz de trazabilidad:** `docs/modelo_de_dominio_y_reglas_de_negocio_yorha_s_a.md`.

## Configuración
Los parámetros de la Ficha (horarios, tiempo de preparación, plazo de confirmación, anticipo, descuento por estancia
prolongada y recargo por llegada nocturna) están en `src/main/resources/application.properties`, con el prefijo
`sga.alojamiento`. Cambiarlos no requiere recompilar.
