package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DescuentoEstanciaProlongada;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio: calcula el valor de una estancia (7.4). Necesita datos de tres agregados que ninguno puede
 * evaluar solo: la temporada de cada noche (Alojamiento), la tarifa del apartamento (Apartamento) y los ocupantes.
 * <ul>
 *   <li>RN-05: el valor es la suma, noche por noche, de la tarifa vigente de esa noche por los ocupantes facturables.</li>
 *   <li>RN-06: es facturable quien a la fecha de ENTRADA alcanza el umbral de edad del alojamiento.</li>
 *   <li>RN-Propia-01: desde cierto número de noches se descuenta un porcentaje de la tarifa de cada noche.</li>
 * </ul>
 * Clase Java pura: se registra como bean en DomainServiceConfig; el dominio no conoce a Spring.
 */
public class CotizadorEstanciaService {

    private final DescuentoEstanciaProlongada descuentoEstanciaProlongada;

    public CotizadorEstanciaService(DescuentoEstanciaProlongada descuentoEstanciaProlongada) {
        this.descuentoEstanciaProlongada = Objects.requireNonNull(descuentoEstanciaProlongada,
                "El descuento por estancia prolongada no puede ser nulo");
    }

    public Cotizacion cotizar(Alojamiento alojamiento, Apartamento apartamento, Estancia estancia, List<Ocupante> ocupantes) {
        Objects.requireNonNull(alojamiento, "El alojamiento no puede ser nulo");
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        Objects.requireNonNull(estancia, "La estancia no puede ser nula");
        Objects.requireNonNull(ocupantes, "Los ocupantes no pueden ser nulos");

        int umbralEdad = alojamiento.parametros().umbralEdadFacturable().anios();
        int facturables = (int) ocupantes.stream()
                .filter(o -> o.esFacturable(estancia.entrada(), umbralEdad))
                .count();
        if (facturables == 0) {
            throw new ReglaDominioException("La estancia debe tener al menos un ocupante facturable (con "
                    + umbralEdad + " años o más a la fecha de entrada)");
        }

        boolean conDescuento = descuentoEstanciaProlongada.aplicaPara(estancia.noches());
        List<DetalleNoche> desglose = new ArrayList<>();
        for (LocalDate noche : estancia.nochesOcupadas()) {
            Temporada temporada = alojamiento.temporadaVigente(noche);
            Dinero tarifa = apartamento.tarifaVigente(temporada.id(), noche).valorPorNoche();
            if (conDescuento) {
                tarifa = descuentoEstanciaProlongada.aplicarA(tarifa);
            }
            desglose.add(new DetalleNoche(noche, temporada.nombre(), tarifa, facturables));
        }
        return new Cotizacion(desglose);
    }
}
