"use client";

import { FormEvent, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { Pantalla } from "@/components/Pantalla";
import { Pasos } from "@/components/Pasos";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { leerArchivos } from "@/lib/formato";
import type { Tramite } from "@/lib/tipos";

const PASOS = ["Datos", "Documentos", "Acuse"];

export default function PaginaConstruccion() {
  const [paso, setPaso] = useState(1);
  const [tipoObra, setTipoObra] = useState("");
  const [clave, setClave] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [archivos, setArchivos] = useState<FileList | null>(null);
  const [tramite, setTramite] = useState<Tramite | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function radicar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const documentos = archivos ? await leerArchivos(archivos) : [];
      const creado = await api<Tramite>("/api/v1/tramites/licencias-construccion", {
        method: "POST",
        body: {
          tipoObra,
          claveCatastral: clave.trim(),
          descripcion,
          documentos,
        },
      });
      setTramite(creado);
      setPaso(3);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-05" titulo="Licencia de construcción">
      <p className="intro">Este trámite se radica sin cobro en pasarela. No hay cargo de tarjeta ni de PSE.</p>
      <Pasos actual={paso} pasos={PASOS} />

      {paso === 1 ? (
        <section className="tarjeta">
          <label htmlFor="tipo">Tipo de obra</label>
          <input id="tipo" name="tipoObra" required placeholder="OBRA_MENOR" value={tipoObra} onChange={(e) => setTipoObra(e.target.value)} />
          <label htmlFor="clave">Clave catastral</label>
          <input
            id="clave"
            name="claveCatastral"
            required
            placeholder="05-021-0147-003"
            value={clave}
            aria-invalid={campoInvalido(error, "claveCatastral")}
            onChange={(e) => setClave(e.target.value)}
          />
          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" name="descripcion" required value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
          <div className="fila-acciones">
            <button type="button" disabled={!tipoObra || !clave || !descripcion} onClick={() => setPaso(2)}>
              Continuar
            </button>
          </div>
        </section>
      ) : null}

      {paso === 2 ? (
        <form className="tarjeta" onSubmit={(evento) => void radicar(evento)}>
          <p>
            {tipoObra} · {clave}
          </p>
          <p>{descripcion}</p>
          <label htmlFor="documentos">Documentos</label>
          <input
            id="documentos"
            name="documentos"
            type="file"
            multiple
            aria-invalid={campoInvalido(error, "documentos")}
            onChange={(e) => setArchivos(e.target.files)}
          />
          <p>Adjunta al menos un archivo, por ejemplo plano.pdf.</p>
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setPaso(1)}>
              Volver
            </button>
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Radicando…" : "Radicar expediente"}
            </button>
          </div>
        </form>
      ) : null}

      {paso === 3 && tramite ? (
        <section className="tarjeta" role="status">
          <p>Expediente radicado. No se generó un cobro.</p>
          <p>
            Estado: <span className="estado">{tramite.estado}</span>
          </p>
          <p className="folio">{tramite.folio}</p>
          <BotonCopiar texto={tramite.folio} />
          <p>{tramite.titulo}</p>
        </section>
      ) : null}
    </Pantalla>
  );
}
