import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import { esquemaBusqueda, type FormularioBusqueda } from '../esquemas/busqueda';
import type { Apartamento } from '../modelos/apartamento';
import type { Pagina } from '../modelos/pagina';
import { buscarDisponibles, type FiltroDisponibilidad } from '../servicios/apartamentoServicio';
import { hoyISO } from '../utilidades/fechas';
import { mensajeDeError } from '../utilidades/errores';


export default function Inicio() {
  // filtro: lo último que el usuario buscó. Cambiarlo (o la página) dispara la consulta al backend.
  const [filtro, setFiltro] = useState<FiltroDisponibilidad | null>(null);
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState<Pagina<Apartamento> | null>(null);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormularioBusqueda>({
    resolver: zodResolver(esquemaBusqueda),
    mode: 'onTouched',
    defaultValues: { fechaEntrada: '', fechaSalida: '', adultos: 2, ninos: 0, conMascota: false },
  });

  useEffect(() => {
    if (!filtro) {
      return;
    }
    // "cancelado" evita pintar la respuesta de una búsqueda vieja si el usuario ya hizo otra
    let cancelado = false;
    setCargando(true);
    setMensaje(null);
    buscarDisponibles(filtro, pagina)
      .then((respuesta) => {
        if (!cancelado) {
          setResultado(respuesta);
        }
      })
      .catch((error: unknown) => {
        if (!cancelado) {
          setResultado(null);
          setMensaje(mensajeDeError(error));
        }
      })
      .finally(() => {
        if (!cancelado) {
          setCargando(false);
        }
      });
    return () => {
      cancelado = true;
    };
  }, [filtro, pagina]);

  function alBuscar(datos: FormularioBusqueda) {
    setPagina(0);
    setFiltro({ ...datos });   // objeto nuevo: repetir la misma búsqueda vuelve a consultar
  }

  return (
    <section>
      <h1>Apartamentos disponibles</h1>

      <form onSubmit={handleSubmit(alBuscar)} noValidate>
        <div>
          <label htmlFor="fechaEntrada">Fecha de entrada</label>
          <input id="fechaEntrada" type="date" min={hoyISO()} {...register('fechaEntrada')} />
          {errors.fechaEntrada && <p className="error">{errors.fechaEntrada.message}</p>}
        </div>

        <div>
          <label htmlFor="fechaSalida">Fecha de salida</label>
          <input id="fechaSalida" type="date" {...register('fechaSalida')} />
          {errors.fechaSalida && <p className="error">{errors.fechaSalida.message}</p>}
        </div>

        <div>
          <label htmlFor="adultos">Adultos</label>
          <input id="adultos" type="number" min={1} {...register('adultos', { valueAsNumber: true })} />
          {errors.adultos && <p className="error">{errors.adultos.message}</p>}
        </div>

        <div>
          <label htmlFor="ninos">Niños</label>
          <input id="ninos" type="number" min={0} {...register('ninos', { valueAsNumber: true })} />
          {errors.ninos && <p className="error">{errors.ninos.message}</p>}
        </div>

        <div>
          <label htmlFor="conMascota">
            <input id="conMascota" type="checkbox" {...register('conMascota')} /> Viajo con mascota
          </label>
        </div>

        <button type="submit" disabled={cargando}>
          {cargando ? 'Buscando…' : 'Buscar'}
        </button>
      </form>

      {mensaje && <p className="error">{mensaje}</p>}

      {resultado && (
        <>
          <p>
            {resultado.page.totalElements} apartamento(s) disponible(s) para el grupo y las fechas indicadas.
          </p>

          {resultado.content.map((apartamento) => (
            <TarjetaApartamento key={apartamento.identificacion} apartamento={apartamento} />
          ))}

          {resultado.page.totalPages > 1 && (
            <nav aria-label="Paginación">
              <button type="button" disabled={cargando || resultado.page.number === 0} onClick={() => setPagina(pagina - 1)}>
                Anterior
              </button>
              {' '}Página {resultado.page.number + 1} de {resultado.page.totalPages}{' '}
              <button
                type="button"
                disabled={cargando || resultado.page.number + 1 >= resultado.page.totalPages}
                onClick={() => setPagina(pagina + 1)}
              >
                Siguiente
              </button>
            </nav>
          )}
        </>
      )}
    </section>
  );
}
