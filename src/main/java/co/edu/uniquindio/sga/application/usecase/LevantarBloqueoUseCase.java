package co.edu.uniquindio.sga.application.usecase;


import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LevantarBloqueoUseCase {
    private final ApartamentoRepository apartamentoRepositorio;

    public LevantarBloqueoUseCase(ApartamentoRepository apartamentoRepositorio) {
        this.apartamentoRepositorio = apartamentoRepositorio;
    }

    @Transactional
    public void ejecutar(String identificacionApartamento, UUID idBloqueo) {
        Apartamento apartamento = apartamentoRepositorio.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el apartamento: " + identificacionApartamento));

        apartamento.levantarBloqueo(idBloqueo);
        apartamentoRepositorio.guardar(apartamento);
    }
}
