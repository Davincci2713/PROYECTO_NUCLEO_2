"use client";

import { FormEvent, useRef, useState, type MouseEvent } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { leerArchivo } from "@/lib/formato";
import type { Incidencia, VistaPrevia } from "@/lib/tipos";

const TIPOS = ["BACHE", "ALUMBRADO", "AGUA", "BASURA"] as const;

export default function PaginaIncidencia() {
  const secuencia = useRef(0);
  const [marca, setMarca] = useState<{ x: number; y: number } | null>(null);
  const [lat, setLat] = useState<number | null>(null);
  const [lng, setLng] = useState<number | null>(null);
  const [direccion, setDireccion] = useState("");
  const [tipo, setTipo] = useState<(typeof TIPOS)[number]>("BACHE");
  const [descripcion, setDescripcion] = useState("");
  const [foto, setFoto] = useState<File | null>(null);
  const [creada, setCreada] = useState<Incidencia | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function ubicar(evento: MouseEvent<HTMLDivElement>) {
    const rect = evento.currentTarget.getBoundingClientRect();
    const x = Math.min(1, Math.max(0, (evento.clientX - rect.left) / rect.width));
    const y = Math.min(1, Math.max(0, (evento.clientY - rect.top) / rect.height));
    const latitud = Number((19.4 + (1 - y) * 0.1).toFixed(6));
    const longitud = Number((-99.2 + x * 0.1).toFixed(6));
    setMarca({ x: x * 100, y: y * 100 });
    setLat(latitud);
    setLng(longitud);
    setCreada(null);
    const turno = ++secuencia.current;
    setError(null);
    try {
      const previa = await api<VistaPrevia>("/api/v1/incidencias/vista-previa", {
        method: "POST",
        body: { lat: latitud, lng: longitud },
      });
      if (turno === secuencia.current) setDireccion(previa.direccionReferencia);
    } catch (err) {
      if (turno === secuencia.current) setError(normalizarError(err));
    }
  }

  async function crear(evento: FormEvent) {
    evento.preventDefault();
    if (lat === null || lng === null) return;
    setError(null);
    setOcupado(true);
    try {
      let fotoBase64: string | null = null;
      let fotoContentType: string | null = null;
      if (foto) {
        const carga = await leerArchivo(foto);
        fotoBase64 = carga.contenidoBase64;
        fotoContentType = carga.contentType;
      }
      const incidencia = await api<Incidencia>("/api/v1/incidencias", {
        method: "POST",
        body: {
          tipo,
          lat,
          lng,
          direccionReferencia: direccion,
          descripcion,
          fotoBase64,
          fotoContentType,
        },
      });
      setCreada(incidencia);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-24" titulo="Reportar incidencia">
      <p className="intro">Pulsa el plano. El punto se convierte en coordenadas y la dirección de referencia queda lista para corregirla.</p>
      <form className="tarjeta" onSubmit={(evento) => void crear(evento)}>
        <span className="etiqueta" id="mapa-ayuda">
          Plano esquemático del municipio
        </span>
        <div
          className="mapa"
          role="application"
          aria-labelledby="mapa-ayuda"
          onClick={(evento) => void ubicar(evento)}
        >
          {marca ? <span className="marca-mapa" style={{ left: `${marca.x}%`, top: `${marca.y}%` }} /> : null}
          <span className="mapa-leyenda">Pulsa para ubicar el reporte</span>
        </div>
        <p>
          Latitud {lat ?? "—"} · Longitud {lng ?? "—"}
        </p>
        <label htmlFor="direccion">Dirección de referencia</label>
        <input
          id="direccion"
          name="direccionReferencia"
          required
          value={direccion}
          aria-invalid={campoInvalido(error, "direccionReferencia") || campoInvalido(error, "lat") || campoInvalido(error, "lng")}
          onChange={(e) => setDireccion(e.target.value)}
        />
        <label htmlFor="tipo">Tipo</label>
        <select id="tipo" name="tipo" value={tipo} onChange={(e) => setTipo(e.target.value as (typeof TIPOS)[number])}>
          {TIPOS.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
        <label htmlFor="descripcion">Descripción</label>
        <textarea id="descripcion" name="descripcion" required value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        <label htmlFor="foto">Fotografía (opcional)</label>
        <input id="foto" name="foto" type="file" accept="image/*" onChange={(e) => setFoto(e.target.files?.[0] ?? null)} />
        <AvisoError error={error} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado || lat === null || lng === null}>
            {ocupado ? "Enviando…" : "Crear reporte"}
          </button>
        </div>
      </form>
      {creada ? (
        <section className="ok" role="status">
          <p>Reporte asignado.</p>
          <p className="folio">{creada.folio}</p>
          <BotonCopiar texto={creada.folio} />
          <p>
            Brigada: {creada.brigada.nombre}
            {creada.brigada.zona ? ` · ${creada.brigada.zona}` : ""}
          </p>
          <p>
            Estado {creada.estado} · {creada.direccionReferencia}
          </p>
        </section>
      ) : null}
    </Pantalla>
  );
}
