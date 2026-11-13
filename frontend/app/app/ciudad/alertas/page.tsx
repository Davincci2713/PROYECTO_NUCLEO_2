"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { esServidor, leerUsuario } from "@/lib/sesion";
import type { EnsayoAlerta, PreferenciasAlerta } from "@/lib/tipos";

const TEMAS = ["SISMO", "INUNDACION", "SERVICIOS", "SALUD"];

export default function PaginaAlertas() {
  const [telefono, setTelefono] = useState("");
  const [temas, setTemas] = useState<string[]>([]);
  const [temaEnsayo, setTemaEnsayo] = useState("SISMO");
  const [mensaje, setMensaje] = useState("");
  const [ensayo, setEnsayo] = useState<EnsayoAlerta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [aviso, setAviso] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [servidor, setServidor] = useState(false);

  useEffect(() => {
    setServidor(esServidor(leerUsuario()?.rol));
    let cancelado = false;
    api<PreferenciasAlerta>("/api/v1/ciudad/alertas/preferencias")
      .then((datos) => {
        if (cancelado) return;
        setTelefono(datos.telefono ?? "");
        setTemas(datos.temas ?? []);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  function alternar(tema: string) {
    setTemas((actual) => (actual.includes(tema) ? actual.filter((item) => item !== tema) : [...actual, tema]));
  }

  async function guardar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setAviso("");
    setOcupado(true);
    try {
      const datos = await api<PreferenciasAlerta>("/api/v1/ciudad/alertas/preferencias", {
        method: "PUT",
        body: { telefono, temas },
      });
      setTelefono(datos.telefono ?? "");
      setTemas(datos.temas ?? []);
      setAviso("Preferencias guardadas.");
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function ensayar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setEnsayo(null);
    setOcupado(true);
    try {
      const datos = await api<EnsayoAlerta>("/api/v1/ciudad/alertas/ensayo", {
        method: "POST",
        body: { tema: temaEnsayo, mensaje },
      });
      setEnsayo(datos);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-23" titulo="Alertas">
      <p className="intro">Elige los temas sobre los que quieres recibir avisos. El ensayo municipal registra destinatarios y no llama a un proveedor de SMS.</p>
      <form className="tarjeta" onSubmit={(evento) => void guardar(evento)}>
        <label htmlFor="telefono">Teléfono</label>
        <input id="telefono" name="telefono" type="tel" value={telefono} onChange={(e) => setTelefono(e.target.value)} />
        <fieldset>
          <legend>Temas</legend>
          {TEMAS.map((tema) => (
            <label className="check" key={tema}>
              <input type="checkbox" name="temas" value={tema} checked={temas.includes(tema)} onChange={() => alternar(tema)} />
              {tema}
            </label>
          ))}
        </fieldset>
        {aviso ? <p className="ok">{aviso}</p> : null}
        <AvisoError error={error} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            Guardar preferencias
          </button>
        </div>
      </form>
      {servidor ? (
        <form className="tarjeta" onSubmit={(evento) => void ensayar(evento)}>
          <h2>Ensayo de alerta</h2>
          <label htmlFor="tema">Tema</label>
          <select id="tema" name="tema" value={temaEnsayo} onChange={(e) => setTemaEnsayo(e.target.value)}>
            {TEMAS.map((tema) => (
              <option key={tema} value={tema}>
                {tema}
              </option>
            ))}
          </select>
          <label htmlFor="mensaje">Mensaje</label>
          <textarea id="mensaje" name="mensaje" required value={mensaje} onChange={(e) => setMensaje(e.target.value)} />
          {ensayo ? (
            <p className="ok" role="status">
              Ensayo de {ensayo.tema}. Destinatarios: {ensayo.destinatarios}.
            </p>
          ) : null}
          <div className="fila-acciones">
            <button type="submit" className="acento" disabled={ocupado}>
              Enviar ensayo
            </button>
          </div>
        </form>
      ) : null}
    </Pantalla>
  );
}
