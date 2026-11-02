package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.SolicitudReserva;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio: valida las condiciones de creación de una reserva y la crea con su valor y su política
 * congelados (RN-22). Junta lo que ningún agregado puede evaluar solo: las reservas existentes del apartamento
 * (Reserva), sus bloqueos y capacidad (Apartamento) y los parámetros y la política (Alojamiento).
 * <ul>
 *   <li>RN-04: la entrada no puede ser anterior a hoy. RN-03 ya la protege Estancia.</li>
 *   <li>3.2 / RN-06: el titular es siempre facturable; nadie puede aparecer dos veces.</li>
 *   <li>RN-02 y RN-Propia-03: los ocupantes (y la mascota, que ocupa un cupo) no pueden exceder la capacidad, y con
 *       mascota el apartamento debe permitir mascotas.</li>
 *   <li>RN-01, RN-07 y RN-20: sin reservas activas solapadas ni bloqueos sobre esas noches, respetando el tiempo de
 *       preparación entre la salida de un grupo y la entrada del siguiente.</li>
 *   <li>RN-05 y RN-Propia-01: el valor sale del cotizador.</li>
 *   <li>RN-Propia-02: una llegada nocturna estimada agrega su cargo al folio.</li>
 * </ul>
 * Clase Java pura: se registra como bean en DomainServiceConfig.
 */
public class CreacionReservaService {

    private final DisponibilidadApartamentoService disponibilidadService;
    private final CotizadorEstanciaService cotizadorService;
    private final GeneradorCodigoReserva generadorCodigo;
    private final RecargoLlegadaNocturna recargoLlegadaNocturna;

    public CreacionReservaService(DisponibilidadApartamentoService disponibilidadService,
                                  CotizadorEstanciaService cotizadorService,
                                  GeneradorCodigoReserva generadorCodigo,
                                  RecargoLlegadaNocturna recargoLlegadaNocturna) {
        this.disponibilidadService = Objects.requireNonNull(disponibilidadService, "El servicio de disponibilidad no puede ser nulo");
        this.cotizadorService = Objects.requireNonNull(cotizadorService, "El cotizador no puede ser nulo");
        this.generadorCodigo = Objects.requireNonNull(generadorCodigo, "El generador de códigos no puede ser nulo");
        this.recargoLlegadaNocturna = Objects.requireNonNull(recargoLlegadaNocturna, "El recargo por llegada nocturna no puede ser nulo");
    }

    /** @param ahora momento de creación; lo entrega quien invoca (nunca el dominio), para poder probar con fechas fijas */
    public Reserva crear(Alojamiento alojamiento, Apartamento apartamento, SolicitudReserva solicitud, LocalDateTime ahora) {
        Objects.requireNonNull(alojamiento, "El alojamiento no puede ser nulo");
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        Objects.requireNonNull(solicitud, "La solicitud no puede ser nula");
        Objects.requireNonNull(ahora, "El momento de creación no puede ser nulo");

        LocalDate hoy = ahora.toLocalDate();
        Estancia estancia = solicitud.estancia();

        // RN-04
        if (estancia.entradaEsAnteriorA(hoy)) {
            throw new ReglaDominioException("RN-04: la fecha de entrada (" + estancia.entrada()
                    + ") no puede ser anterior a la fecha actual (" + hoy + ")");
        }

        // 3.2: el titular cuenta como ocupante; nadie puede estar dos veces
        List<Ocupante> ocupantes = new ArrayList<>();
        ocupantes.add(solicitud.titular());
        ocupantes.addAll(solicitud.acompanantes());
        long documentosDistintos = ocupantes.stream().map(Ocupante::documento).distinct().count();
        if (documentosDistintos != ocupantes.size()) {
            throw new ReglaDominioException("Un ocupante no puede aparecer dos veces en la reserva (documento repetido)");
        }

        // 3.2 / RN-06: el titular es siempre facturable
        int umbralEdad = alojamiento.parametros().umbralEdadFacturable().anios();
        if (!solicitud.titular().esFacturable(estancia.entrada(), umbralEdad)) {
            throw new ReglaDominioException("3.2: el titular debe tener " + umbralEdad
                    + " años o más a la fecha de entrada (es siempre un ocupante facturable)");
        }

        // RN-02 / RN-Propia-03: todos cuentan para la capacidad, y la mascota ocupa un cupo
        apartamento.verificarAdmiteGrupo(ocupantes.size(), solicitud.conMascota());

        // RN-01 / RN-07 / RN-20: sin reservas activas solapadas ni bloqueos, y apartamento disponible para la venta
        if (!disponibilidadService.estaDisponible(apartamento, estancia, ocupantes.size(), solicitud.conMascota(),
                alojamiento.parametros())) {
            throw new ReglaDominioException("RN-01 / RN-07 / RN-20: el apartamento " + apartamento.identificacion()
                    + " no está disponible del " + estancia.entrada() + " al " + estancia.salida()
                    + " (tiene una reserva activa o un bloqueo en esas noches, no alcanza el tiempo de preparación"
                    + " con otra reserva, o está retirado de la venta)");
        }

        // RN-05 / RN-Propia-01 y RN-22: el valor y la política quedan congelados
        Cotizacion cotizacion = cotizadorService.cotizar(alojamiento, apartamento, estancia, ocupantes);
        VersionPolitica versionPolitica = alojamiento.politicaVigente(hoy).numeroVersion();

        Reserva reserva = new Reserva(generadorCodigo.generar(), apartamento.identificacion(), solicitud.titular(), estancia,
                solicitud.canalOrigen(), cotizacion, versionPolitica, ahora, solicitud.conMascota());
        ocupantes.forEach(reserva::agregarOcupante);

        // RN-09 / RN-Propia-02
        if (solicitud.horaEstimadaLlegada() != null) {
            reserva.registrarHoraEstimadaLlegada(solicitud.horaEstimadaLlegada(), recargoLlegadaNocturna);
        }
        return reserva;
    }
}
