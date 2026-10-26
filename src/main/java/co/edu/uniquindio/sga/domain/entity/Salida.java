package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Salida {
    private LocalDate fechaSalida;
    private LocalTime horaSalida;
    private Reserva reservaAsignada;
    private Apartamento apartamentoAsignado;
    private Titular titularAsignado;

    public Salida(LocalDate fechaSalida, LocalTime horaSalida, Reserva reservaAsignada, Apartamento apartamentoAsignado, Titular titularAsignado) {
        if (fechaSalida == null) {
            throw new ReglaDominioException("La fecha de entrada no puede ser vacia");
        }

        if (horaSalida == null) {
            throw new ReglaDominioException("La hora de entrada no puede ser vacia");
        }

        if (reservaAsignada == null) {
            throw new ReglaDominioException("La reserva no puede ser vacia");
        }

        if (apartamentoAsignado == null) {
            throw new ReglaDominioException("El apartamento no puede ser vacio");
        }

        if (titularAsignado == null) {
            throw new ReglaDominioException("El titular no puede ser vacio");
        }
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
        this.reservaAsignada = reservaAsignada;
        this.apartamentoAsignado = apartamentoAsignado;
        this.titularAsignado = titularAsignado;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public Reserva getReservaAsignada() {
        return reservaAsignada;
    }

    public Apartamento getApartamentoAsignado() {
        return apartamentoAsignado;
    }

    public Titular getTitularAsignado() {
        return titularAsignado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Salida salida)) return false;
        return Objects.equals(fechaSalida, salida.fechaSalida);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fechaSalida);
    }
}
