package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Verifica que el usuario autenticado sea el titular de la reserva (Guía 12, 7.2) */
@Component("propietarioReserva")
@RequiredArgsConstructor
public class PropietarioReservaVerificador {

    private final ReservaRepository reservaRepository;

    public boolean esTitular(String codigo, Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        String documentoToken = jwt.getClaimAsString("documento");

        return reservaRepository.obtenerPorCodigo(new CodigoReserva(codigo))
                .map(reserva -> reserva.getTitular().documento().numero().equals(documentoToken))
                .orElse(false);
    }
}