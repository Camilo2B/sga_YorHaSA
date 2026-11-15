import { Link } from 'react-router';


export default function NoAutorizado() {
  return (
    <section className="text-center py-5">
      <i className="bi bi-shield-lock display-4 text-danger" aria-hidden="true"></i>
      <h1 className="h3 mt-3">Acceso no autorizado</h1>
      <p className="text-body-secondary">Su rol no tiene permiso para ver esta página.</p>
      <Link to="/" className="btn btn-primary">Ir al inicio</Link>
    </section>
  );
}
