# SGA - Sistema de Gestión de Alojamiento
## Búnker Orbital YoRHa - Suites & Apartamentos Temáticos

Sistema de gestión de alojamiento desarrollado como proyecto académico. Su propósito es facilitar la administración y operación de un establecimiento de suites y apartamentos temáticos, incluyendo la gestión de apartamentos, reservas, tarifas, temporadas, disponibilidad y personal.

La aplicación está construida con **Java y Spring Boot** para el backend, y **React con TypeScript** para el frontend.

---

## Integrantes del equipo

- Tomás Castaño Ortiz
- Camilo Ruiz López

---

## Enlaces del proyecto

| Recurso | Enlace |
|---|---|
| **Aplicación web (Frontend)** | https://sgayorhasa.vercel.app |
| **API (Backend)** | https://sga-yorhasa-api-camilo-ruiz-tomas-castano.onrender.com |

---

## Tecnologías utilizadas

### Backend
- Java 25
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT para autenticación
- Gradle
- Base de datos H2

### Frontend
- React 19
- TypeScript
- Vite
- React Router
- Node.js y npm

---

## Requisitos previos

Antes de ejecutar el proyecto localmente, asegúrese de tener instaladas las siguientes herramientas:

- **Java Development Kit (JDK):** versión 25.
- **Node.js y npm:** necesarios para ejecutar el frontend.
- **Git:** para clonar el repositorio.
- **Gradle:** no es necesario instalarlo, ya que el proyecto incluye Gradle Wrapper.

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/Camilo2B/sga_YorHaSA.git
cd sga_YorHaSA
```

### 2. Ejecutar las pruebas del backend

Desde la raíz del proyecto, ejecute:

```bash
./gradlew test
```

En Windows PowerShell:

```powershell
.\gradlew.bat test
```

El reporte de pruebas se genera en:

```text
build/reports/tests/test/index.html
```

### 3. Iniciar el backend

Desde la raíz del proyecto:

```bash
./gradlew bootRun
```

En Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

Una vez iniciado, la API estará disponible en:

http://localhost:8080

Al iniciar la aplicación se cargan automáticamente datos iniciales de prueba, que incluyen:

- El alojamiento y sus parámetros de configuración.
- Las temporadas y la política de cancelación.
- Los 15 apartamentos definidos en la Ficha, junto con sus tarifas.
- Un bloqueo de apartamento.
- Tres reservas en diferentes estados: pendiente, confirmada y cancelada.

### 4. Iniciar el frontend

Abra una **segunda terminal**, manteniendo el backend en ejecución.

Desde la raíz del proyecto, acceda a la carpeta del frontend:

```bash
cd frontend
```

Instale las dependencias (si es la primera ejecución):

```bash
npm install
```

Inicie el servidor de desarrollo:

```bash
npm run dev
```

Vite mostrará en la terminal la dirección local desde la cual podrá acceder a la aplicación web.

---

## Documentación y herramientas de la API

El proyecto incluye documentación y recursos para consultar y probar los servicios del backend.

| Recurso | Ubicación |
|---|---|
| **Contrato OpenAPI** | `docs/api/openapi.yaml` |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Colección de pruebas HTTP** | `docs/api/apartamentos.http` |
| **Consola de H2** | http://localhost:8080/h2-console |
| **Modelo de dominio y reglas de negocio** | `docs/modelo_de_dominio_y_reglas_de_negocio_yorha_s_a.md` |

### Consola de H2

Para acceder a la consola de la base de datos durante la ejecución local, utilice:

- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:sgadb`
- **Usuario:** `sa`
- **Contraseña:** dejar vacía, salvo que la configuración local indique lo contrario.

### Pruebas HTTP

El archivo `docs/api/apartamentos.http` contiene solicitudes para probar los endpoints del sistema mediante el cliente HTTP integrado de IntelliJ IDEA.

Se recomienda ejecutar las solicitudes en el orden definido y con la aplicación iniciada.

---

## Configuración del alojamiento

Los parámetros de configuración del alojamiento se encuentran en:

```text
src/main/resources/application.properties
```

Se identifican mediante el prefijo:

```properties
sga.alojamiento
```

Entre los parámetros configurables se encuentran:

- Horarios de operación.
- Tiempo de preparación de apartamentos.
- Plazo de confirmación de reservas.
- Porcentaje de anticipo.
- Descuento por estancia prolongada.
- Recargo por llegada nocturna.

Estos valores pueden modificarse desde el archivo de configuración sin necesidad de recompilar el proyecto.

---

## Ejecución en desarrollo

Para trabajar localmente, es necesario mantener ambos servidores en ejecución:

1. **Backend:** ejecutado desde la raíz del proyecto mediante `./gradlew bootRun`.
2. **Frontend:** ejecutado desde la carpeta `frontend` mediante `npm run dev`.

De esta manera, el frontend podrá interactuar con los servicios expuestos por el backend según la configuración del entorno.

---

**Búnker Orbital YoRHa - Suites & Apartamentos Temáticos**  
*Sistema de Gestión de Alojamiento (SGA)*