import { useEffect, useState, type ChangeEvent } from 'react';
import { Link, useParams } from 'react-router';
import { subirImagenes } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito } from '../utilidades/notificaciones';


const MAXIMO_IMAGENES = 10;               // 7.3: mínimo 1, máximo 10
const TAMANO_MAXIMO = 5 * 1024 * 1024;    // 5 MB por imagen
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp'];


interface Vista {
    archivo: File;
    url: string;   // URL temporal del navegador para mostrar la miniatura
}


/** Devuelve el primer problema encontrado, o null si los archivos son válidos. */
function validar(archivos: File[]): string | null {
    if (archivos.length === 0) return 'Seleccione al menos una imagen';
    if (archivos.length > MAXIMO_IMAGENES) return `Máximo ${MAXIMO_IMAGENES} imágenes por apartamento`;
    const invalido = archivos.find((a) => !TIPOS_PERMITIDOS.includes(a.type));
    if (invalido) return `${invalido.name}: solo se aceptan JPG, PNG o WEBP`;
    const pesado = archivos.find((a) => a.size > TAMANO_MAXIMO);
    if (pesado) return `${pesado.name} pesa más de 5 MB`;
    return null;
}


export default function SubirImagenes() {
    const { identificacion = '' } = useParams();
    const [vistas, setVistas] = useState<Vista[]>([]);
    const [principal, setPrincipal] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [progreso, setProgreso] = useState<number | null>(null);


    // Las URL temporales ocupan memoria hasta que se liberan. La limpieza se ejecuta
    // cuando cambian las vistas (libera las anteriores) y al salir de la página.
    useEffect(() => {
        return () => vistas.forEach((v) => URL.revokeObjectURL(v.url));
    }, [vistas]);


    function alSeleccionar(evento: ChangeEvent<HTMLInputElement>) {
        // files es una FileList (parecida a un arreglo): Array.from la convierte
        const archivos = Array.from(evento.target.files ?? []);
        const problema = validar(archivos);
        setError(problema);
        setPrincipal(0);
        setVistas(problema ? [] : archivos.map((archivo) => ({ archivo, url: URL.createObjectURL(archivo) })));
    }


    async function alSubir() {
        setProgreso(0);
        try {
            await subirImagenes(identificacion, vistas.map((v) => v.archivo), principal, setProgreso);
            notificarExito(`${vistas.length} imagen(es) subidas a ${identificacion}`);
            setVistas([]);
        } catch (e) {
            notificarError(mensajeDeError(e));
        } finally {
            setProgreso(null);
        }
    }


    return (
        <section>
            <h1 className="h3">Imágenes de {identificacion}</h1>
            <p className="text-body-secondary">Entre 1 y 10 imágenes JPG, PNG o WEBP de hasta 5 MB. Marque la principal.</p>


            <div className="mb-3">
                <label htmlFor="imagenes" className="form-label">Imágenes</label>
                <input
                    id="imagenes"
                    type="file"
                    multiple
                    accept={TIPOS_PERMITIDOS.join(',')}
                    className={`form-control ${error ? 'is-invalid' : ''}`}
                    onChange={alSeleccionar}
                />
                <div className="invalid-feedback">{error}</div>
            </div>


            <div className="row g-3 mb-3">
                {vistas.map((vista, indice) => (
                    <div key={vista.url} className="col-6 col-md-3">
                        <div className={`card h-100 ${indice === principal ? 'border-primary border-2' : ''}`}>
                            <img src={vista.url} alt={vista.archivo.name} className="card-img-top object-fit-cover" style={{ height: '8rem' }} />
                            <div className="card-body p-2">
                                <div className="form-check">
                                    <input
                                        id={`principal-${indice}`}
                                        type="radio"
                                        name="principal"
                                        className="form-check-input"
                                        checked={indice === principal}
                                        onChange={() => setPrincipal(indice)}
                                    />
                                    <label htmlFor={`principal-${indice}`} className="form-check-label small">Principal</label>
                                </div>
                                <small className="text-body-secondary d-block text-truncate">{vista.archivo.name}</small>
                            </div>
                        </div>
                    </div>
                ))}
            </div>


            {progreso !== null && (
                <div className="progress mb-3" role="progressbar" aria-label="Progreso de la subida" aria-valuenow={progreso} aria-valuemin={0} aria-valuemax={100}>
                    <div className="progress-bar" style={{ width: `${progreso}%` }}>{progreso}%</div>
                </div>
            )}


            <div className="d-flex gap-2">
                <button type="button" className="btn btn-primary" disabled={vistas.length === 0 || progreso !== null} onClick={alSubir}>
                    <i className="bi bi-cloud-upload me-1" aria-hidden="true"></i>Subir
                </button>
                <Link to="/administracion" className="btn btn-outline-secondary">Volver</Link>
            </div>
        </section>
    );
}
