package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de dominio son clases Java puras (no conocen a Spring); aquí se registran como beans.
 * Se irán agregando a medida que sus casos de uso lleguen a la API.
 * Pendiente: DisponibilidadApartamentoService y GestionBloqueoService (necesitan el adaptador JPA de Reserva),
 * y los parámetros de la Ficha (sga.alojamiento.*) cuando existan los servicios que los usan.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public RegistroApartamentoService registroApartamentoService(ApartamentoRepository apartamentoRepository) {
        return new RegistroApartamentoService(apartamentoRepository);
    }
}
