# Modelo de Dominio, Reglas de Negocio y Arquitectura — YorHa S.A

Documentación técnica del Sistema de Gestión de Alojamiento (SGA) para el Búnker Orbital YoRHa - Suites & Apartamentos Temáticos.

> Actualizado con la Entrega 2: los paquetes y las clases corresponden al código (`co.edu.uniquindio.sga`), y la matriz de trazabilidad indica el endpoint que expone cada regla. El contrato completo de la API está en `docs/api/openapi.yaml`.

---

## 1. Identidades y Objetos de Valor (Pestaña 1: Identidades)

| Concepto del Dominio | Tipo de Concepto (Entidad / Objeto Valor) | Justificación de Diseño | Paquete (Arquitectura Limpia) | Atributos Principales | Reglas de Negocio / Invariantes que Protege |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Alojamiento** | Entidad (Raíz de Agregado) | Representa la configuración global y única de la propiedad orbital. Contiene las temporadas y el historial de versiones de la política de cancelación. | `domain.entity` | `id`, `nombre`, `ciudad`, `parametros`, `temporadas`, `historialPoliticas` | RN-13, RN-20, RN-21, RN-22 |
| **Apartamento** | Entidad (Raíz de Agregado) | Tiene identidad inmutable (`identificacion`) y su propio ciclo de vida operativo. Contiene sus tarifas, bloqueos y novedades. | `domain.entity` | `identificacion`, `nombre`, `capacidad`, `dormitorios`, `permiteMascotas`, `estadoOperativo`, `activo` | RN-01, RN-02, RN-07, RN-11, RN-20, RN-Propia-03 |
| **Reserva** | Entidad (Raíz de Agregado) | Tiene identidad inmutable (`codigo`), ciclo de vida de estados y administra su propio folio. Referencia al apartamento solo por su identificación. | `domain.entity` | `codigo`, `apartamentoId`, `estancia`, `titular`, `ocupantes`, `estado`, `canalOrigen`, `cotizacion`, `versionPolitica`, `folio` | RN-01, RN-04, RN-08, RN-10, RN-12, RN-13, RN-21, RN-22 |
| **Folio** | Entidad (dentro del agregado Reserva) | Administra la cuenta de la reserva; acumula cargos y pagos de forma auditable. | `domain.entity` | `id`, `reservaId`, `cargos`, `pagos` | RN-16, RN-17 |
| **Novedad** | Entidad (dentro de Apartamento / Reserva) | Reporte registrado con id único sobre eventos o daños en un apartamento. | `domain.entity` | `id`, `fecha`, `autor`, `descripcion`, `gravedad` | Regla Propia (IA de clasificación) |
| **Bloqueo** | Entidad (dentro del agregado Apartamento) | Registro administrativo que deshabilita la venta de un apartamento en un rango de fechas. | `domain.entity` | `id`, `rango`, `motivo` | RN-07 |
| **Tarifa** | Entidad (dentro del agregado Apartamento) | Versión del valor por noche de un apartamento en una temporada; se conserva el histórico. | `domain.entity` | `id`, `temporadaId`, `valorPorNoche`, `vigenteDesde` | RN-05 |
| **Estancia** | Objeto de Valor (Value Object) | Define el rango continuo de noches `[entrada, salida)`. Inmutable y sin identidad propia. | `domain.valueobject` | `entrada`, `salida` | RN-01, RN-03, RN-20 |
| **Dinero** | Objeto de Valor (Value Object) | Representa un valor en COP con precisión exacta sin decimales. | `domain.valueobject` | `monto` | Definición 3.4 del Dominio |
| **Ocupante** | Entidad (dentro del agregado Reserva) | Persona que se alojará; se distingue por su documento (nadie aparece dos veces en una reserva). | `domain.entity` | `documento`, `nombre`, `fechaNacimiento` | RN-06 |
| **Cargo** | Entidad (dentro de Folio) | Débito inmutable en el folio. | `domain.entity` | `id`, `tipo`, `valor` | RN-16 |
| **Pago** | Entidad (dentro de Folio) | Abono inmutable en el folio. | `domain.entity` | `id`, `valor`, `medio`, `fecha` | RN-16 |

---

## 2. Relación de Reglas de Negocio (Pestaña 2: Relación de reglas)

| Código Regla | Descripción Breve de la Regla | Entidad / Servicio Responsable | Invariante o Validacion principal |
| :--- | :--- | :--- | :--- |
| **RN-01** | Sin traslape de reservas activas sobre un mismo apartamento. | `DisponibilidadApartamentoService` / `Apartamento` | `!estanciaA.seSolapaCon(estanciaB)` |
| **RN-02** | Capacidad máxima del apartamento es un tope rígido. | `Apartamento` / `RegistroApartamentoService` | `totalOcupantes <= capacidadMaxima` |
| **RN-03** | Estancia mínima de 1 noche; fecha salida no se cobra. | `Estancia` | `fechaSalida > fechaEntrada` |
| **RN-04** | Prohibido crear reservas con fecha de entrada en el pasado. | `CreacionReservaService` | `fechaEntrada >= fechaActual` |
| **RN-05** | Cobro por ocupante facturable noche a noche según temporada. | `CotizadorEstanciaService` | `subtotal = sum(tarifaNoche * numFacturables)` |
| **RN-06** | Todos cuentan para capacidad, solo mayores al umbral pagan. | `Ocupante` / `CotizadorEstanciaService` | `edadEnEntrada >= umbralEdad` |
| **RN-07** | Apartamento bloqueado no está disponible para venta. | `GestionBloqueoService` / `Apartamento` | `!tieneBloqueoVigente(rangoFechas)` |
| **RN-08** | Transiciones estricta del ciclo de vida de la reserva. | `Reserva` | Transición válida según autómata de estados |
| **RN-10** | Precondiciones de registro (check-in) obligatorias. | `EntregaApartamentoService` / `Reserva` | `estado == CONFIRMADA && fechaActual == fechaEntrada` |
| **RN-11** | Registro requiere apartamento en estado `PREPARADO`. | `Apartamento` / `EntregaApartamentoService` | `estadoOperativo == PREPARADO` |
| **RN-12** | Cancelación o No-Show libera disponibilidad de inmediato. | `Reserva` | `estado in [CANCELADA, NO_SHOW] -> liberaNoches()` |
| **RN-13** | Retención de cancelación según versión congelada. | `PoliticaCancelacion` / `Reserva` | Aplica versión congelada en la reserva |
| **RN-16** | Cargos y pagos inmutables; correcciones mediante inversa. | `Folio` | Sin `delete` ni `update` en movimientos de folio |
| **RN-17** | Cierre de folio exige saldo igual a cero (o autoriz. admin). | `Folio` / `Reserva` | `saldo == 0 || autorizadoPorAdmin` |
| **RN-18** | Choque de canal externo se rechaza y registra como conflicto. | `ConflictoCanal` (canal externo, fuera del alcance del corte 2) | Registrar conflicto en log de auditoría |
| **RN-19** | Idempotencia en reservas de canales externos. | Canal externo (fuera del alcance del corte 2) | `unique(canal, idExterno)` |
| **RN-20** | Tiempo de preparación entre salida y entrada. | `DisponibilidadApartamentoService` / `HorarioAlojamiento` | `diferenciaHoras >= tiempoPreparacionConfig` |
| **RN-21** | Plazo límite para confirmación y cobro de anticipo. | `Reserva` (lo dispara `CancelarReservasVencidasUseCase`) | Expiración automática si no confirma en X horas |
| **RN-22** | Congelamiento de tarifas y políticas al crear la reserva. | `CreacionReservaService` / `Reserva` | Copia inmutable del desglose de cotización |
| **RN-Propia-01** | Descuento del 10% para estancias de 5 o más noches. | `CotizadorEstanciaService` / `DescuentoEstanciaProlongada` | `estancia.noches >= 5 -> descuento(0.10)` |
| **RN-Propia-02** | Recargo de $30.000 COP por recepción nocturna (22:00 a 05:00). | `Reserva` / `RecargoLlegadaNocturna` | `horaLlegada in [22:00, 05:00] -> aplicarCargo(30000)` |
| **RN-Propia-03** | Presencia de mascota reduce en 1 cupo la capacidad física. | `Apartamento` | `tieneMascota -> permiteMascotas && capacidadEfectiva = capacidad - 1` |

---

## 3. Catálogo de Servicios de Dominio (Pestaña 3: Catálogo de servicios)

Los servicios resuelven solo las reglas que ningún agregado puede evaluar solo. Son clases Java puras, registradas como `@Bean` en `infrastructure.config.DomainServiceConfig`. La columna "Nombre en la Entrega 1" relaciona cada servicio con el nombre con que se diseñó.

| Nombre del Servicio | Nombre en la Entrega 1 | Responsabilidad del Dominio | Casos de Uso / Flujos que Atiende | Reglas de Negocio que Ejecuta |
| :--- | :--- | :--- | :--- | :--- |
| **`CotizadorEstanciaService`** | `ServicioCotizacion` | Liquida el costo total de una estancia noche por noche considerando temporadas y ocupantes facturables. | `CotizarEstanciaUseCase`, `CrearReservaUseCase` | RN-03, RN-05, RN-06, RN-Propia-01 |
| **`DisponibilidadApartamentoService`** | `ServicioDisponibilidad` | Valida si un apartamento está libre y apto para el grupo en un rango de fechas, cruzando sus bloqueos y capacidad con las reservas activas. | `BuscarApartamentosDisponiblesUseCase`, `CrearReservaUseCase` | RN-01, RN-02, RN-07, RN-20, RN-Propia-03 |
| **`CreacionReservaService`** | `ServicioGestionReserva` (creación) | Valida las condiciones de creación de una reserva y la crea con su valor y su política congelados. | `CrearReservaUseCase` | RN-01, RN-02, RN-04, RN-06, RN-07, RN-20, RN-22, RN-Propia-02, RN-Propia-03 |
| **`EntregaApartamentoService`** | `ServicioOperativoApartamento` | Decide si se puede entregar el apartamento a una reserva (registro / check-in). | `RegistrarLlegadaUseCase` | RN-10, RN-11 |
| **`GestionBloqueoService`** | — (nuevo en la Entrega 2) | Registra un bloqueo solo si no hay reservas activas en esas noches. | `BloquearApartamentoUseCase` | RN-07 |
| **`RegistroApartamentoService`** | — (nuevo en la Entrega 2) | Administra el inventario: identificación única, retiro de la venta y cambio de capacidad respetando las reservas activas. | `CrearApartamentoUseCase`, `RetirarApartamentoUseCase`, `CambiarCapacidadApartamentoUseCase` | L-04, 7.3, RN-02 |

Las operaciones que la Entrega 1 asignaba a `ServicioGestionReserva` (confirmar, cancelar, no-show, vencimiento) y a `ServicioFolio` (cargos, pagos, cierre) quedaron como métodos de los agregados `Reserva` y `Folio`: cada una se resuelve con los datos del propio agregado más la política congelada, así que no necesitan un servicio. `ServicioClasificacionNovedades` (IA) pertenece a la operación diaria y no se exige en el corte 2.

---

## 4. Matriz de Trazabilidad (Pestaña 4: Matriz de trazabilidad)

| Requerimiento / Funcionalidad | Caso de Uso / Capa Aplicación | Entidad / Servicio de Dominio | Reglas Asociadas | Capa de Infraestructura / API |
| :--- | :--- | :--- | :--- | :--- |
| **Registro de apartamento** | `CrearApartamentoUseCase` | `Apartamento`, `RegistroApartamentoService` | L-04, RN-02 | `POST /api/apartamentos` |
| **Consulta del inventario** | `ListarApartamentosUseCase`, `ConsultarApartamentoUseCase` | `Apartamento` | — | `GET /api/apartamentos`, `GET /api/apartamentos/{identificacion}` |
| **Cambio de capacidad** | `CambiarCapacidadApartamentoUseCase` | `Apartamento`, `RegistroApartamentoService` | RN-02 | `PUT /api/apartamentos/{identificacion}/capacidad` |
| **Retiro de la venta (eliminación lógica)** | `RetirarApartamentoUseCase` | `Apartamento`, `RegistroApartamentoService` | 7.3, 12.4 | `PUT /api/apartamentos/{identificacion}/retirar` |
| **Bloqueo administrativo** | `BloquearApartamentoUseCase`, `ListarBloqueosApartamentoUseCase`, `ConsultarBloqueoUseCase` | `Bloqueo`, `Apartamento`, `GestionBloqueoService` | RN-03, RN-07 | `POST /api/apartamentos/{id}/bloquear`, `GET /api/apartamentos/{identificacion}/bloqueos`, `GET /api/apartamentos/{identificacion}/bloqueos/{id}` |
| **Preparación del apartamento** | `IniciarPreparacionApartamentoUseCase`, `MarcarApartamentoPreparadoUseCase` | `Apartamento`, `EstadoOperativo` | 7.6 | `PUT /api/apartamentos/{identificacion}/iniciar-preparacion`, `PUT /api/apartamentos/{identificacion}/marcar-preparado` |
| **Alojamiento y temporadas** | `ConsultarAlojamientoUseCase`, `RegistrarTemporadaUseCase`, `ConsultarTemporadaUseCase` | `Alojamiento`, `Temporada` | RN-03, 7.4 | `GET /api/alojamiento`, `POST /api/alojamiento/temporadas`, `GET /api/alojamiento/temporadas/{id}` |
| **Tarifas** | `DefinirTarifaUseCase`, `ListarTarifasApartamentoUseCase`, `ConsultarTarifaUseCase` | `Apartamento`, `Tarifa` | RN-05, 7.4 | `POST /api/apartamentos/{identificacion}/tarifas`, `GET /api/apartamentos/{identificacion}/tarifas`, `GET /api/apartamentos/{identificacion}/tarifas/{id}` |
| **Política de cancelación** | `DefinirPoliticaCancelacionUseCase`, `ListarPoliticasCancelacionUseCase`, `ConsultarPoliticaCancelacionUseCase` | `Alojamiento`, `PoliticaCancelacion` | RN-13, 7.4 | `POST /api/alojamiento/politica-cancelacion`, `GET /api/alojamiento/politica-cancelacion`, `GET /api/alojamiento/politica-cancelacion/{version}` |
| **Búsqueda de disponibilidad** | `BuscarApartamentosDisponiblesUseCase` | `DisponibilidadApartamentoService`, `Apartamento` | RN-01, RN-02, RN-03, RN-07, RN-20, RN-Propia-03 | `GET /api/apartamentos/disponibles` |
| **Cotización** | `CotizarEstanciaUseCase` | `CotizadorEstanciaService` | RN-03, RN-05, RN-06, RN-Propia-01 | `POST /api/cotizaciones` |
| **Creación de Reserva** | `CrearReservaUseCase` | `Reserva`, `CreacionReservaService`, `DisponibilidadApartamentoService`, `CotizadorEstanciaService` | RN-01, RN-02, RN-03, RN-04, RN-06, RN-07, RN-20, RN-22, RN-Propia-02, RN-Propia-03 | `POST /api/reservas` |
| **Consulta y listado de reservas** | `ConsultarReservaUseCase`, `ConsultarReservasUseCase` | `Reserva` | 7.5 | `GET /api/reservas/{codigo}`, `GET /api/reservas` |
| **Confirmación y anticipo** | `ConfirmarReservaUseCase` | `Reserva`, `Folio`, `PoliticaAnticipo` | RN-08, RN-09, RN-16, RN-21, RN-Propia-02 | `PUT /api/reservas/{codigo}/confirmar` |
| **Vencimiento del plazo de confirmación** | `CancelarReservasVencidasUseCase` (tarea programada) | `Reserva`, `PlazoConfirmacion` | RN-12, RN-21 | `ReservasVencidasScheduler` (sin endpoint) |
| **Cancelación de Reserva** | `CancelarReservaUseCase` | `Reserva`, `PoliticaCancelacion` | RN-08, RN-12, RN-13 | `PUT /api/reservas/{codigo}/cancelar` |
| **Proceso de Check-in** | `RegistrarLlegadaUseCase` | `Reserva`, `Apartamento`, `EntregaApartamentoService` | RN-08, RN-10, RN-11 | `PUT /api/reservas/{codigo}/registrar-llegada` |
| **Proceso de Check-out** | `RegistrarSalidaUseCase` | `Reserva`, `Apartamento`, `Folio` | RN-08, RN-17 | `PUT /api/reservas/{codigo}/registrar-salida` |
| **No-show** | `DeclararNoShowUseCase`, `DeclararNoShowsVencidosUseCase` (tarea programada) | `Reserva`, `PoliticaCancelacion`, `HoraLimiteNoShow` | RN-08, RN-12, RN-13 | `PUT /api/reservas/{codigo}/no-show`, `NoShowScheduler` |
| **Folio y pagos** | `ConsultarFolioUseCase`, `RegistrarPagoUseCase` | `Folio`, `Cargo`, `Pago` | RN-16 | `GET /api/reservas/{codigo}/folio`, `POST /api/reservas/{codigo}/folio/pagos` |
| **Reporte de Novedades (IA)** | `ReportarNovedadUseCase` (operación diaria, fuera del alcance del corte 2) | `Novedad`, `ServicioClasificacionNovedades` | Caso IA del Equipo | Pendiente para un corte posterior |

---

## 5. Endpoints API REST (Pestaña 5: APIs)

Todos los listados se paginan de 10 en 10 (`page`, `size`) y responden con `content` + `page`. Los errores usan un único `ErrorResponse`: 400 forma del mensaje inválida, 404 recurso inexistente, 409 regla de negocio violada.

| Método HTTP | Endpoint API | Descripción | Parámetros de Entrada / Body | Código Respuesta HTTP |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/apartamentos` | Registra un apartamento. | Body: `identificacion`, `nombre`, `capacidad`, `dormitorios`, `estadoOperativoInicial`, `permiteMascotas` | `201 Created` + `Location`, `400`, `409` |
| `GET` | `/api/apartamentos` | Lista el inventario (activos y retirados). | `page`, `size` | `200 OK` |
| `GET` | `/api/apartamentos/disponibles` | Busca apartamentos disponibles por fechas y composición del grupo. | `fechaEntrada`, `fechaSalida`, `adultos`, `ninos`, `conMascota`, `page`, `size` | `200 OK`, `400`, `409` |
| `GET` | `/api/apartamentos/{identificacion}` | Consulta un apartamento. | — | `200 OK`, `404` |
| `PUT` | `/api/apartamentos/{identificacion}/capacidad` | Cambia la capacidad. | Body: `capacidad` | `200 OK`, `400`, `404`, `409` |
| `PUT` | `/api/apartamentos/{identificacion}/retirar` | Retira el apartamento de la venta (eliminación lógica). | — | `200 OK`, `404`, `409` |
| `POST` | `/api/apartamentos/{id}/bloquear` | Registra un bloqueo administrativo en un rango de fechas. | Body: `fechaInicio`, `fechaFin`, `motivo` | `201 Created` + `Location`, `400`, `404`, `409` |
| `GET` | `/api/apartamentos/{identificacion}/bloqueos` | Lista los bloqueos del apartamento. | `page`, `size` | `200 OK`, `404` |
| `GET` | `/api/apartamentos/{identificacion}/bloqueos/{id}` | Consulta un bloqueo. | — | `200 OK`, `400`, `404` |
| `POST` | `/api/apartamentos/{identificacion}/tarifas` | Define una nueva versión de tarifa para una temporada. | Body: `temporadaId`, `valorPorNoche`, `vigenteDesde` | `201 Created` + `Location`, `400`, `404`, `409` |
| `GET` | `/api/apartamentos/{identificacion}/tarifas` | Histórico de tarifas del apartamento. | `temporadaId`, `page`, `size` | `200 OK`, `400`, `404` |
| `GET` | `/api/apartamentos/{identificacion}/tarifas/{id}` | Consulta una tarifa. | — | `200 OK`, `400`, `404` |
| `PUT` | `/api/apartamentos/{identificacion}/iniciar-preparacion` | `PENDIENTE_PREPARACION` → `EN_PREPARACION`. | — | `200 OK`, `404`, `409` |
| `PUT` | `/api/apartamentos/{identificacion}/marcar-preparado` | `EN_PREPARACION` → `PREPARADO`. | — | `200 OK`, `404`, `409` |
| `GET` | `/api/alojamiento` | Consulta el alojamiento, sus parámetros, temporadas y política vigente. | — | `200 OK`, `404` |
| `POST` | `/api/alojamiento/temporadas` | Registra una temporada. | Body: `nombre`, `fechaInicio`, `fechaFin` | `201 Created` + `Location`, `400`, `404`, `409` |
| `GET` | `/api/alojamiento/temporadas/{id}` | Consulta una temporada. | — | `200 OK`, `400`, `404` |
| `POST` | `/api/alojamiento/politica-cancelacion` | Define una nueva versión de la política de cancelación. | Body: `tramos`, `vigenteDesde` | `201 Created` + `Location`, `400`, `404`, `409` |
| `GET` | `/api/alojamiento/politica-cancelacion` | Lista las versiones de la política. | `page`, `size` | `200 OK`, `404` |
| `GET` | `/api/alojamiento/politica-cancelacion/{version}` | Consulta una versión de la política. | — | `200 OK`, `400`, `404` |
| `POST` | `/api/cotizaciones` | Cotiza una estancia noche por noche. | Body: `identificacionApartamento`, `fechaEntrada`, `fechaSalida`, `ocupantes` | `200 OK`, `400`, `404`, `409` |
| `POST` | `/api/reservas` | Registra una nueva reserva en estado `PENDIENTE`. | Body: titular, ocupantes, apartamento, estancia, canal, `conMascota`, `horaEstimadaLlegada` | `201 Created` + `Location`, `400`, `404`, `409` |
| `GET` | `/api/reservas` | Lista reservas, de la más reciente a la más antigua. | `estado`, `identificacionApartamento`, `desde`, `hasta`, `page`, `size` | `200 OK`, `400` |
| `GET` | `/api/reservas/{codigo}` | Consulta una reserva. | — | `200 OK`, `404` |
| `PUT` | `/api/reservas/{codigo}/confirmar` | Cobra el anticipo y pasa la reserva a `CONFIRMADA`. | Body opcional: `horaEstimadaLlegada`, `medioPago`, `referencia` | `200 OK`, `400`, `404`, `409` |
| `PUT` | `/api/reservas/{codigo}/cancelar` | Cancela aplicando la política congelada. | Body: `motivo` | `200 OK`, `400`, `404`, `409` |
| `PUT` | `/api/reservas/{codigo}/registrar-llegada` | Check-in: la reserva pasa a `EN_CURSO` y el apartamento a `OCUPADO`. | — | `200 OK`, `404`, `409` |
| `PUT` | `/api/reservas/{codigo}/registrar-salida` | Check-out con el folio en cero. | — | `200 OK`, `404`, `409` |
| `PUT` | `/api/reservas/{codigo}/no-show` | Declara el no-show pasada la hora límite. | — | `200 OK`, `404`, `409` |
| `GET` | `/api/reservas/{codigo}/folio` | Consulta el folio. | — | `200 OK`, `404` |
| `POST` | `/api/reservas/{codigo}/folio/pagos` | Registra un pago en el folio. | Body: `valor`, `medioPago`, `referencia` | `201 Created`, `400`, `404`, `409` |
