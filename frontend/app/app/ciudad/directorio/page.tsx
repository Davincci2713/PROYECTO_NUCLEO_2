"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import type { Pyme } from "@/lib/tipos";

export default function PaginaDirectorio() {
  const [q, setQ] = useState("");
  const [pymes, setPymes] = useState<Pyme[] | null>(null);
  const [nombre, setNombre] = useState("");
  const [giro, setGiro] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [telefono, setTelefono] = useState("");
  const [colonia, setColonia] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function buscar(consulta: string) {
    const ruta = consulta.trim() ? `/api/v1/ciudad/pymes?q=${encodeURIComponent(consulta.trim())}` : "/api/v1/ciudad/pymes?q=";
    const lista = await api<Pyme[]>(ruta);
    setPymes(lista);
  }

  useEffect(() => {
    let cancelado = false;
    buscar("")
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  async function alBuscar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await buscar(q);
    } catch (err) {
      setPymes(null);
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function alta(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api<Pyme>("/api/v1/ciudad/pymes", {
        method: "POST",
        body: { nombre, giro, descripcion, telefono, colonia },
      });
      setNombre("");
      setGiro("");
      setDescripcion("");
      setTelefono("");
      setColonia("");
      await buscar(q);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-21" titulo="Directorio de negocios">
      <form className="tarjeta" onSubmit={(evento) => void alBuscar(evento)}>
        <label htmlFor="q">Buscar por nombre, giro o colonia</label>
        <input id="q" name="q" value={q} onChange={(e) => setQ(e.target.value)} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            Buscar
          </button>
        </div>
      </form>
      <AvisoError error={error} />
      {pymes && pymes.length === 0 ? <p>No hay negocios para esa búsqueda.</p> : null}
      {(pymes ?? []).map((pyme) => (
        <article key={pyme.id} className="tarjeta">
          <h2>{pyme.nombre}</h2>
          <p>
            {pyme.giro} · {pyme.colonia}
          </p>
          <p>{pyme.descripcion}</p>
          <p>Teléfono {pyme.telefono}</p>
        </article>
      ))}
      <form className="tarjeta" onSubmit={(evento) => void alta(evento)}>
        <h2>Registrar negocio</h2>
        <label htmlFor="nombre">Nombre</label>
        <input id="nombre" name="nombre" required value={nombre} onChange={(e) => setNombre(e.target.value)} />
        <label htmlFor="giro">Giro</label>
        <input id="giro" name="giro" required value={giro} onChange={(e) => setGiro(e.target.value)} />
        <label htmlFor="descripcion">Descripción</label>
        <textarea id="descripcion" name="descripcion" required value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        <label htmlFor="telefono">Teléfono</label>
        <input id="telefono" name="telefono" required value={telefono} onChange={(e) => setTelefono(e.target.value)} />
        <label htmlFor="colonia">Colonia</label>
        <input id="colonia" name="colonia" required value={colonia} onChange={(e) => setColonia(e.target.value)} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            Dar de alta
          </button>
        </div>
      </form>
    </Pantalla>
  );
}
