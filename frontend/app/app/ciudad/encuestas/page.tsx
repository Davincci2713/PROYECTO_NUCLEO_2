"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import { esServidor, leerUsuario } from "@/lib/sesion";
import type { Encuesta } from "@/lib/tipos";

export default function PaginaEncuestas() {
  const [encuestas, setEncuestas] = useState<Encuesta[] | null>(null);
  const [votos, setVotos] = useState<Record<string, string>>({});
  const [pregunta, setPregunta] = useState("");
  const [opciones, setOpciones] = useState(["", ""]);
  const [dias, setDias] = useState("15");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [servidor, setServidor] = useState(false);

  function cargar() {
    return api<Encuesta[]>("/api/v1/ciudad/encuestas").then(setEncuestas);
  }

  useEffect(() => {
    setServidor(esServidor(leerUsuario()?.rol));
    let cancelado = false;
    cargar().catch((err) => {
      if (!cancelado) setError(normalizarError(err));
    });
    return () => {
      cancelado = true;
    };
  }, []);

  async function votar(id: string, evento: FormEvent) {
    evento.preventDefault();
    const opcionId = votos[id];
    if (!opcionId) return;
    setError(null);
    setOcupado(true);
    try {
      await api<void>(`/api/v1/ciudad/encuestas/${id}/votar`, { method: "POST", body: { opcionId } });
      setEncuestas((lista) => lista?.map((item) => (item.id === id ? { ...item, yaVote: true } : item)) ?? lista);
      await cargar();
    } catch (err) {
      const cuerpo = normalizarError(err);
      setError(cuerpo);
      if (cuerpo.codigo === "YA_VOTO") {
        setEncuestas((lista) => lista?.map((item) => (item.id === id ? { ...item, yaVote: true } : item)) ?? lista);
      }
    } finally {
      setOcupado(false);
    }
  }

  async function crear(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api("/api/v1/ciudad/encuestas", {
        method: "POST",
        body: { pregunta, opciones: opciones.map((item) => item.trim()).filter(Boolean), dias: Number(dias) },
      });
      setPregunta("");
      setOpciones(["", ""]);
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-20" titulo="Encuestas">
      <p className="intro">Cada cuenta puede votar una sola vez. Si ya votaste, las opciones quedan cerradas.</p>
      <AvisoError error={error} />
      {(encuestas ?? []).map((encuesta) => {
        const bloqueada = encuesta.yaVote || encuesta.cerrada;
        return (
          <form key={encuesta.id} className="tarjeta" onSubmit={(evento) => void votar(encuesta.id, evento)}>
            <h2>{encuesta.pregunta}</h2>
            <p>
              Cierra {fecha(encuesta.cierraEn)}
              {encuesta.cerrada ? " · Cerrada" : ""}
              {encuesta.yaVote ? " · Ya registraste tu voto" : ""}
            </p>
            <fieldset disabled={bloqueada}>
              <legend>Opciones</legend>
              {encuesta.opciones.map((opcion) => (
                <label className="check" key={opcion.id}>
                  <input
                    type="radio"
                    name={`voto-${encuesta.id}`}
                    value={opcion.id}
                    checked={votos[encuesta.id] === opcion.id}
                    onChange={() => setVotos((actual) => ({ ...actual, [encuesta.id]: opcion.id }))}
                  />
                  <span>
                    {opcion.texto} · {opcion.votos} votos
                  </span>
                </label>
              ))}
            </fieldset>
            <button type="submit" disabled={bloqueada || ocupado || !votos[encuesta.id]}>
              Votar
            </button>
          </form>
        );
      })}
      {encuestas && encuestas.length === 0 ? <p>No hay encuestas abiertas.</p> : null}

      {servidor ? (
        <form className="tarjeta" onSubmit={(evento) => void crear(evento)}>
          <h2>Nueva encuesta</h2>
          <label htmlFor="pregunta">Pregunta</label>
          <input id="pregunta" name="pregunta" required value={pregunta} onChange={(e) => setPregunta(e.target.value)} />
          {opciones.map((opcion, indice) => (
            <div key={indice}>
              <label htmlFor={`opcion-${indice}`}>Opción {indice + 1}</label>
              <input
                id={`opcion-${indice}`}
                value={opcion}
                onChange={(e) => setOpciones((actual) => actual.map((item, i) => (i === indice ? e.target.value : item)))}
              />
            </div>
          ))}
          <label htmlFor="dias">Días abierta</label>
          <input id="dias" name="dias" type="number" min="1" required value={dias} onChange={(e) => setDias(e.target.value)} />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setOpciones((actual) => [...actual, ""])}>
              Agregar opción
            </button>
            <button type="submit" disabled={ocupado}>
              Publicar encuesta
            </button>
          </div>
        </form>
      ) : null}
    </Pantalla>
  );
}
