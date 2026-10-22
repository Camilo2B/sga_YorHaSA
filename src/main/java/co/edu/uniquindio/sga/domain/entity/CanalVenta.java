package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;

import java.util.Objects;

public class CanalVenta {

    private String identificacion;
    private String nombre;
    //El alojamiento vende por los 3 canales definidos
    private CanalOrigen origen;
    private Boolean activo;

    public CanalVenta(String identificacion, String nombre, CanalOrigen origen, Boolean activo) {
        if (identificacion == null) {
            throw new ReglaDominioException("El canal de venta debe tener una identificacion");
        }

        if (nombre == null || nombre.isBlank()){
            throw new ReglaDominioException("El canal de venta debe tener un nombre");

        }

        if (origen == null){
            throw new ReglaDominioException("El canal de venta debe tener un tipo de origen");

        }
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.origen = origen;
        this.activo = activo;
    }

    //Falta metodo para que solo sea en tales canales de origen

    public void verificarCanalOrigen() {
        if (!activo) {
            throw new ReglaDominioException("El canal de venta no esta activo y debe seleccionar otro canal origen");
        }
    }

    public String getIdentificacion() {
        return identificacion;
    }


    public String getNombre() {
        return nombre;
    }


    public CanalOrigen getOrigen() {
        return origen;
    }


    public Boolean getActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CanalVenta)) return false;
        return Objects.equals(identificacion, ((CanalVenta) o).identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }

}
