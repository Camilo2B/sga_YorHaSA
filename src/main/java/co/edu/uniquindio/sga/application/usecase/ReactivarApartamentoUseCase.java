package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReactivarApartamentoUseCase {
    private final ApartamentoRepository apartamentoRepositorio;

    public ReactivarApartamentoUseCase(ApartamentoRepository apartamentoRepositorio) {
        this.apartamentoRepositorio = apartamentoRepositorio;
    }

    @Transactional
    public void ejecutar(String identificacion) {
        Apartamento apartamento = apartamentoRepositorio.buscarPorId(identificacion)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el apartamento: " + identificacion));

        apartamento.reactivar();
        apartamentoRepositorio.guardar(apartamento);
    }
}
