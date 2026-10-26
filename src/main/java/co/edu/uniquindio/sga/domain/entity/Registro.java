package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;


public class Registro {
    private LocalDate fechaEntrada;
    private LocalTime horaEntrada;
    private Reserva reservaAsignada;
    private Apartamento apartamentoAsignado;
    private Titular titularAsignado;

    public Registro(LocalDate fechaEntrada, LocalTime horaEntrada, Reserva reservaAsignada, Apartamento apartamentoAsignado, Titular titularAsignado) {

        if (fechaEntrada == null) {
            throw new ReglaDominioException("La fecha de entrada no puede ser vacia");
        }

        if (horaEntrada == null) {
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
        this.fechaEntrada = fechaEntrada;
        this.horaEntrada = horaEntrada;
        this.reservaAsignada = reservaAsignada;
        this.apartamentoAsignado = apartamentoAsignado;
        this.titularAsignado = titularAsignado;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
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
        if (!(o instanceof Registro registro)) return false;
        return Objects.equals(fechaEntrada, registro.fechaEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fechaEntrada);
    }
}
