import { Link } from 'react-router';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';


export default function Administracion() {
    return (
        <section>
            <h1 className="h3">Administración del alojamiento</h1>
            <p className="text-body-secondary">
                Apartamentos, temporadas, tarifas y parámetros (7.1). Solo para el rol ADMINISTRADOR.
            </p>


            <div className="list-group">
                {apartamentosEjemplo.map((apartamento) => (
                    <div key={apartamento.identificacion} className="list-group-item d-flex justify-content-between align-items-center">
                        <span>{apartamento.identificacion} · {apartamento.nombre}</span>
                        <Link
                            to={`/administracion/apartamentos/${apartamento.identificacion}/imagenes`}
                            className="btn btn-sm btn-outline-primary"
                        >
                            <i className="bi bi-images me-1" aria-hidden="true"></i>Imágenes
                        </Link>
                    </div>
                ))}
            </div>
        </section>
    );
}
