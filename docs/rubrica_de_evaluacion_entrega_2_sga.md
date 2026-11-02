# Rúbrica de Evaluación — Entrega 2 · SGA
**Asignatura:** Programación Avanzada (12332)  
**Proyecto:** SGA — Sistema de Gestión de Alojamiento  
**Evaluación:** Corte 2  

La Entrega 2 se basa en la codificación del backend del proyecto: el dominio modelado en la Entrega 1 implementado completo, sus casos de uso materializados con Spring Boot, expuestos por una API REST, persistidos en H2, consultables con paginación y protegidos con JWT. Cubre lo trabajado en las guías 04 a 12.

---

## Parte 1 — Elementos exigidos para la Entrega 2

En esta entrega no hay cantidades fijas: el mínimo lo pone la documentación que cada equipo presentó en la Entrega 1 (identificación de conceptos, agregados, servicios de dominio, casos de uso, `openapi.yaml` y reglas propias). Lo que se revisa es que lo documentado esté implementado y funcionando, con las correcciones acordadas en la sustentación de la Entrega 1.

| Elemento | Qué se exige |
| ----- | ----- |
| **Entidades y Objetos de Valor** | Todos los conceptos clasificados en la hoja Identificación de la Entrega 1, implementados en `domain`. Entidades con constructor controlado, sin setters y con igualdad por identidad. Objetos de Valor como `record` o `enum`, inmutables y con validación en el constructor. El dominio sin anotaciones de Spring, JPA ni Lombok. |
| **Agregados e invariantes** | Los agregados del mapa de la Entrega 1, cada uno con su raíz, protegiendo en código todas las invariantes documentadas (RN-XX y L-XX). Referencias entre agregados solo por identificador. Transiciones de estado con métodos de negocio, nunca con un `cambiarEstado` genérico. |
| **Servicios de dominio** | Los servicios del Catálogo de Servicios del Dominio implementados, resolviendo solo las reglas que ningún agregado puede evaluar solo. |
| **Repository** | Una interfaz (puerto) en `domain.repository` por cada agregado que se guarda o se consulta, con los métodos que piden los casos de uso. |
| **Casos de uso** | Todos los casos de uso documentados en la Entrega 1 dentro del alcance del corte 2, implementados en `application.usecase`. Reciben tipos del dominio y orquestan: ninguna regla de negocio vive en ellos. |
| **API: respuestas y DTOs (Request y Response)** | DTOs como `record` en `application.dto.request` y `application.dto.response`, sin exponer tipos del dominio distintos de los `enum` cerrados. Cada operación responde con el código HTTP correcto: 201 con `Location` al crear, 200 en consultas y operaciones de negocio. |
| **Controller, Mapper y Adapters (Bean Validation)** | Un controller por recurso con exactamente los endpoints del `openapi.yaml`. Controllers delgados: validan el request con `@Valid`, convierten DTO ↔ dominio con MapStruct y delegan en el caso de uso. Sin endpoint genérico de cambio de estado y sin DELETE físicos. |
| **Excepciones** | Cada excepción en la capa que le corresponde: la del dominio para las reglas violadas, las de aplicación para recursos inexistentes, y un `@RestControllerAdvice` en infraestructura que las traduce a 400, 401, 403, 404 y 409 con un único `ErrorResponse`. Nunca una traza técnica al cliente. |
| **Materialización de los casos de uso** | Las anotaciones correspondientes: `@Service` y `@Transactional` en los casos de uso, servicios de dominio registrados como `@Bean` en una clase `@Configuration` (el dominio sigue sin conocer a Spring), y los parámetros de la Ficha (sección 6) en `application.properties`, sin valores del negocio quemados en el código. |
| **Persistencia H2 (base de datos, Entity y Mapper)** | Cada Repository con su adaptador JPA: entidades JPA separadas del dominio, `@Embeddable` para los Objetos de Valor, mapper persistencia ↔ dominio y repositorio Spring Data. Eliminación lógica (12.4). Al arrancar, la base queda con los datos de prueba de la sección 12.3. |
| **Consultas y paginación** | Búsqueda de apartamentos disponibles por fechas y composición del grupo, con sus filtros (7.5). Listados de reservas con filtros y de la más reciente a la más antigua. Paginación de 10 por página en todos los listados. |
| **Seguridad con JWT** | Usuarios en H2 con contraseña cifrada (BCrypt), inicio de sesión que emite un JWT con expiración y registro público de huéspedes (7.2). Cada endpoint protegido según la matriz de roles de 7.1 y los recursos del huésped verificados por propietario. La clave del JWT fuera del repositorio. |
| **Documentación** | `docs/api/openapi.yaml` coherente con lo implementado, Swagger UI funcionando con el botón Authorize, colección de pruebas de la API (Postman, Bruno o `.http`) en el repositorio, diagrama de clases actualizado si el dominio cambió y Matriz de Trazabilidad ampliada con el endpoint que expone cada regla. |

---

### Qué incluye cada elemento (para que no queden dudas)

* **Alcance del corte 2:** Usuarios y seguridad (7.2), alojamiento y apartamentos (7.3), temporadas, tarifas y política (7.4), disponibilidad y reservas (7.5). La operación diaria, el folio y el canal externo pueden avanzar si ya los tienen, pero no se exigen en este corte.
* **Dominio:** Es el de la Entrega 1 completo, no solo lo necesario para que respondan los endpoints. Si en la sustentación de la Entrega 1 se acordaron correcciones, se revisan aplicadas. Las pruebas unitarias siguen en verde con `./gradlew test`.
* **Casos de uso:** El número lo fija su propia Entrega 1. Si al implementar descubrieron que faltaba uno o que sobraba, actualicen la documentación: lo que se penaliza es la diferencia entre lo documentado y lo implementado, no el cambio.
* **Controllers y contrato:** El `openapi.yaml` es el contrato. Si un endpoint cambió al implementarlo, el archivo se actualiza en el mismo commit. Un endpoint en Swagger UI que no está en el `openapi.yaml` (o al revés) cuenta como incoherencia.
* **Bean Validation frente a reglas:** Las anotaciones del DTO validan la forma del mensaje (400); las reglas del negocio se validan en el dominio (409). Una regla que aparece solo en un DTO, un controller, un caso de uso o una consulta JPQL no cuenta como implementada aunque el endpoint responda bien.
* **Persistencia:** Persistir no cambia el dominio. Si para guardar un agregado tuvieron que agregarle setters, un constructor vacío o anotaciones de JPA, el diseño de la persistencia está mal. Los adaptadores en memoria pueden quedarse en el repositorio, pero no deben ser los que usa la aplicación al arrancar.
* **Datos de prueba:** Se cargan solos al iniciar la aplicación. No vale insertarlos a mano en la consola de H2 antes de la sustentación.
* **Seguridad:** El rol no basta. Cada recurso con dueño se verifica también por propietario; se probará en vivo con dos huéspedes distintos. Ningún secreto en el historial de commits.
* **Colección de pruebas:** Debe poder ejecutarse completa contra la aplicación recién arrancada, sin preparar nada a mano: flujo completo de una reserva, un 409 por cada regla que exponga la API y los 401 y 403. Es la que se usa en la sustentación.

---

## Parte 2 — Rúbrica de calificación: Backend

| Criterio | % de la entrega | Qué se evalúa |
| ----- | ----- | ----- |
| **Dominio (entidades, objetos de valor, agregados, servicios y repository)** | 15% | El dominio documentado en la Entrega 1 está completo en código, puro (sin framework), con las invariantes protegidas en los agregados y los servicios resolviendo solo lo que ningún agregado puede. |
| **Casos de uso y su materialización** | 15% | Están todos los casos de uso del alcance, orquestan sin reglas escondidas, usan las anotaciones correctas y los parámetros de la Ficha se cambian en la configuración sin recompilar. |
| **API, respuestas y DTOs** | 10% | Endpoints coherentes con el `openapi.yaml` y nombrados con el lenguaje ubicuo, DTOs que no filtran el dominio y códigos HTTP correctos en cada operación. |
| **Controller, mapper y Bean Validation** | 10% | Controllers delgados que solo validan, traducen y delegan; mappers sin lógica; validaciones de forma en el DTO y de negocio en el dominio. |
| **Excepciones** | 10% | Cada excepción en su capa y cada error traducido a su código con significado de negocio, en un formato único y sin trazas técnicas. |
| **Persistencia H2** | 15% | Un adaptador por cada Repository, dominio intacto, Objetos de Valor bien mapeados, eliminación lógica y datos de prueba completos al iniciar. |
| **Consultas y paginación** | 5% | Búsqueda de disponibilidad con sus filtros, listados filtrados y ordenados, y paginación de 10 por página. |
| **Seguridad con JWT** | 10% | Login y registro funcionando, contraseñas cifradas, endpoints protegidos por rol, verificación por propietario y clave fuera del repositorio. |
| **Documentación** | 5% | Contrato, Swagger UI, colección de pruebas y Matriz de Trazabilidad al día con el código. |
| **Organización (Git)** | 5% | Commits distribuidos en el tiempo y contribución verificable de todos los integrantes. |
| **Total** | **100%** | |