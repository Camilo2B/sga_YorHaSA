import { useState } from 'react';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';


export default function Inicio() {
  const [personas, setPersonas] = useState(2);


  // Valor derivado: se calcula en cada render a partir del estado, no se guarda aparte.
  const queCaben = apartamentosEjemplo.filter((a) => a.capacidad >= personas);


  return (
      <section>
        <h1 className="h3 mb-3">Apartamentos</h1>


        <div className="row g-2 align-items-center mb-3">
          <div className="col-auto">
            <label htmlFor="personas" className="col-form-label">Personas:</label>
          </div>
          <div className="col-auto">
            <input
                id="personas"
                type="number"
                min={1}
                className="form-control"
                value={personas}
                onChange={(e) => setPersonas(Number(e.target.value))}
            />
          </div>
        </div>


        <p className="text-body-secondary">
          {queCaben.length} apartamento(s) admiten {personas} persona(s).
        </p>


        {/* Grid: 1 columna en móvil, 2 desde md, 3 desde lg */}
        <div className="row g-3">
          {queCaben.map((apartamento) => (
              <div key={apartamento.identificacion} className="col-12 col-md-6 col-lg-4">
                <TarjetaApartamento apartamento={apartamento} />
              </div>
          ))}
        </div>
      </section>
  );
}
