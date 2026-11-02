package co.edu.uniquindio.sga.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Arma la página pedida a partir de una lista ya filtrada y ordenada (los listados son de 10 por página). */
final class Paginacion {

    private Paginacion() {
    }

    static <T> Page<T> paginar(List<T> elementos, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(elementos);
        }
        int desde = (int) Math.min(pageable.getOffset(), elementos.size());
        int hasta = Math.min(desde + pageable.getPageSize(), elementos.size());
        return new PageImpl<>(elementos.subList(desde, hasta), pageable, elementos.size());
    }
}
