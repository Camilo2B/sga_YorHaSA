package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;

import java.util.List;

public class Reserva {

    private final CodigoReserva codigo;
    private final CanalOrigen canalOrigen;
    private final FechaCreacion fechaCreacion;
    private final Ocupante ocupante;

    private EstadoReserva estadoReserva;
    private Apartamento apartamento;
    private Estancia estancia;
    private List<Ocupante> ocupantes;

    public Reserva(CodigoReserva codigo, CanalOrigen canalOrigen, FechaCreacion fechaCreacion, Ocupante ocupante, EstadoReserva estadoReserva, Apartamento apartamento, Estancia estancia, List<Ocupante> ocupantes) {
        this.codigo = codigo;
        this.canalOrigen = canalOrigen;
        this.fechaCreacion = fechaCreacion;
        this.ocupante = ocupante;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = ocupantes;

        estado = EstadoReserva.PENDIENTE;
    }

    public Reserva crear (CodigoReserva codigo, CanalOrigen canalOrigen, FechaCreacion fechaCreacion, Ocupante ocupante, Apartamento apartamento, Estancia estancia, List<Ocupante> ocupantes) {
        if (codigo == null) {
            throw new ReglaDominioException("La reserva debe contener el código");
        }

        if (canalOrigen == null) {
            throw new ReglaDominioException("La reserva debe tener un canal de origen");


        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("La reserva debe tener al menos un ocupante");

        }
        //terminar validaciones de negocio

        return new Reserva(codigo, canalOrigen, fechaCreacion, ocupantes, apartamento, estancia, ocupante);
    }


}
