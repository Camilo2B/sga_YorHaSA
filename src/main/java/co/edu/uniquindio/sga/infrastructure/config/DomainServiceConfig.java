package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.CreacionReservaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.service.GestionBloqueoService;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.DescuentoEstanciaProlongada;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Registra como beans los servicios de dominio (clases Java puras, que no conocen a Spring) y los parámetros de la
 * Ficha. Los valores salen de application.properties: cambiarlos no requiere recompilar.
 */
@Configuration
@EnableConfigurationProperties(AlojamientoProperties.class)
public class DomainServiceConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    @Bean
    public ParametrosAlojamiento parametrosAlojamiento(AlojamientoProperties propiedades) {
        return new ParametrosAlojamiento(
                new UmbralEdadFacturable(propiedades.umbralEdadFacturable()),
                new HorarioAlojamiento(LocalTime.parse(propiedades.horaEntrada()), LocalTime.parse(propiedades.horaSalida())),
                new TiempoPreparacion(propiedades.tiempoPreparacionHoras()),
                new PlazoConfirmacion(propiedades.plazoConfirmacionHoras()),
                new HoraLimiteNoShow(LocalTime.parse(propiedades.horaLimiteNoShow())),
                new PoliticaAnticipo(propiedades.exigeAnticipo(), propiedades.porcentajeAnticipo())
        );
    }

    @Bean
    public DescuentoEstanciaProlongada descuentoEstanciaProlongada(AlojamientoProperties propiedades) {
        return new DescuentoEstanciaProlongada(propiedades.nochesDescuentoEstanciaProlongada(), propiedades.porcentajeDescuentoEstanciaProlongada());
    }

    @Bean
    public DisponibilidadApartamentoService disponibilidadApartamentoService(ApartamentoRepository apartamentoRepository,
                                                                             ReservaRepository reservaRepository) {
        return new DisponibilidadApartamentoService(apartamentoRepository, reservaRepository);
    }

    @Bean
    public CotizadorEstanciaService cotizadorEstanciaService(DescuentoEstanciaProlongada descuentoEstanciaProlongada) {
        return new CotizadorEstanciaService(descuentoEstanciaProlongada);
    }

    /** RN-Propia-02: el valor y la franja del recargo por llegada nocturna salen de la configuración. */
    @Bean
    public RecargoLlegadaNocturna recargoLlegadaNocturna(AlojamientoProperties propiedades) {
        return new RecargoLlegadaNocturna(Dinero.de(propiedades.recargoLlegadaNocturna()),
                LocalTime.parse(propiedades.horaInicioLlegadaNocturna()), LocalTime.parse(propiedades.horaFinLlegadaNocturna()));
    }

    @Bean
    public CreacionReservaService creacionReservaService(DisponibilidadApartamentoService disponibilidadService,
                                                         CotizadorEstanciaService cotizadorService,
                                                         GeneradorCodigoReserva generadorCodigo,
                                                         RecargoLlegadaNocturna recargoLlegadaNocturna) {
        return new CreacionReservaService(disponibilidadService, cotizadorService, generadorCodigo, recargoLlegadaNocturna);
    }

    @Bean
    public RegistroApartamentoService registroApartamentoService(ApartamentoRepository apartamentoRepository,
                                                                 ReservaRepository reservaRepository) {
        return new RegistroApartamentoService(apartamentoRepository, reservaRepository);
    }

    @Bean
    public GestionBloqueoService gestionBloqueoService(ReservaRepository reservaRepository) {
        return new GestionBloqueoService(reservaRepository);
    }

    @Bean
    public EntregaApartamentoService entregaApartamentoService() {
        return new EntregaApartamentoService();
    }

}
