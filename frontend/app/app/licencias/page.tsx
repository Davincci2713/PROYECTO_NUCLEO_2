"use client";

import { FormEvent, useEffect, useState } from "react";
import { AcusePago } from "@/components/AcusePago";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { FormularioCobro } from "@/components/FormularioCobro";
import { LineaTiempoVista } from "@/components/LineaTiempoVista";
import { Pantalla } from "@/components/Pantalla";
import { Pasos } from "@/components/Pasos";
import { BotonCopiar } from "@/components/BotonCopiar";
import { FranjaImporte } from "@/components/ResumenPago";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero } from "@/lib/formato";
import type { CobroRespuesta, Cotizacion, Giro, Liquidacion, Tramite } from "@/lib/tipos";

const PASOS = ["Cotización", "Solicitud", "Pago", "Recibo"];

export default function PaginaLicencias() {
  const [paso, setPaso] = useState(1);
  const [giros, setGiros] = useState<Giro[] | null>(null);
  const [giro, setGiro] = useState("");
  const [superficie, setSuperficie] = useState("");
  const [nombre, setNombre] = useState("");
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [tramite, setTramite] = useState<Tramite | null>(null);
  const [folioConsulta, setFolioConsulta] = useState("");
  const [liquidacion, setLiquidacion] = useState<Liquidacion | null>(null);
  const [cobro, setCobro] = useState<CobroRespuesta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [errorPago, setErrorPago] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  useEffect(() => {
    let cancelado = false;
    api<Giro[]>("/api/v1/pagos/cotizaciones/giros")
      .then((lista) => {
        if (!cancelado) setGiros(lista);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  useEffect(() => {
    if (!giro || superficie.trim() === "") {
      setCotizacion(null);
      return;
    }
    const metros = Number(superficie);
    if (Number.isNaN(metros)) {
      setCotizacion(null);
      return;
    }
    let cancelado = false;
    const reloj = window.setTimeout(() => {
      void api<Cotizacion>("/api/v1/pagos/cotizaciones/licencia", {
        method: "POST",
        body: { giro, superficieM2: metros },
      })
        .then((datos) => {
          if (cancelado) return;
          setCotizacion(datos);
          setError(null);
        })
        .catch((err) => {
          if (cancelado) return;
          setCotizacion(null);
          setError(normalizarError(err));
        });
    }, 350);
    return () => {
      cancelado = true;
      window.clearTimeout(reloj);
    };
  }, [giro, superficie]);

  async function solicitar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const creado = await api<Tramite>("/api/v1/tramites/licencias-funcionamiento", {
        method: "POST",
        body: { nombreComercial: nombre, giro, superficieM2: Number(superficie) },
      });
      setTramite(creado);
      setFolioConsulta(creado.folio);
      setPaso(3);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function consultarFolio(evento?: FormEvent) {
    evento?.preventDefault();
    const folio = (folioConsulta || tramite?.folio || "").trim();
    if (!folio) return;
    setErrorPago(null);
    setOcupado(true);
    try {
      const datos = await api<Tramite>(`/api/v1/tramites/folio/${encodeURIComponent(folio)}`);
      setTramite(datos);
      if (datos.estado === "APROBADO_PENDIENTE_PAGO" && datos.liquidacionId) {
        const liq = await api<Liquidacion>(`/api/v1/pagos/liquidaciones/${datos.liquidacionId}`);
        setLiquidacion(liq);
      } else {
        setLiquidacion(null);
      }
    } catch (err) {
      setErrorPago(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  const concepto = liquidacion?.concepto ?? (nombre ? `Licencia de funcionamiento ${nombre}` : "Licencia de funcionamiento");
  const importe = liquidacion?.total ?? cotizacion?.total ?? null;

  return (
    <Pantalla hu="HU-03" titulo="Licencia de funcionamiento">
      <p className="intro">La cotización se actualiza al cambiar el giro o la superficie. El cobro queda disponible cuando el trámite está aprobado y pendiente de pago.</p>
      <Pasos actual={paso} pasos={PASOS} />
      <FranjaImporte concepto={concepto} importe={importe} />

      {paso === 1 ? (
        <section className="tarjeta">
          <label htmlFor="giro">Giro</label>
          <select
            id="giro"
            name="giro"
            value={giro}
            aria-invalid={campoInvalido(error, "giro")}
            onChange={(e) => setGiro(e.target.value)}
          >
            <option value="">Elige un giro</option>
            {(giros ?? []).map((item) => (
              <option key={item.giro} value={item.giro}>
                {item.giro} · base {dinero(item.tarifaBase)} · factor {dinero(item.factorM2)}
              </option>
            ))}
          </select>
          <label htmlFor="superficie">Superficie (m²)</label>
          <input
            id="superficie"
            name="superficieM2"
            type="number"
            min="0"
            step="0.01"
            placeholder="40"
            value={superficie}
            aria-invalid={campoInvalido(error, "superficieM2")}
            onChange={(e) => setSuperficie(e.target.value)}
          />
          {cotizacion ? (
            <dl className="datos">
              <div>
                <dt>Tarifa base</dt>
                <dd>{dinero(cotizacion.tarifaBase)}</dd>
              </div>
              <div>
                <dt>Factor por m²</dt>
                <dd>{dinero(cotizacion.factorM2)}</dd>
              </div>
              <div>
                <dt>Superficie</dt>
                <dd>{cotizacion.superficieM2} m²</dd>
              </div>
              <div>
                <dt>Total</dt>
                <dd>{dinero(cotizacion.total)}</dd>
              </div>
            </dl>
          ) : null}
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="button" disabled={!cotizacion} onClick={() => setPaso(2)}>
              Continuar
            </button>
          </div>
        </section>
      ) : null}

      {paso === 2 ? (
        <form className="tarjeta" onSubmit={(evento) => void solicitar(evento)}>
          <label htmlFor="nombre">Nombre comercial</label>
          <input id="nombre" name="nombreComercial" required value={nombre} onChange={(e) => setNombre(e.target.value)} />
          <p>
            Giro {giro}. Superficie {superficie} m².
            {cotizacion ? ` Total cotizado ${dinero(cotizacion.total)}.` : ""}
          </p>
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setPaso(1)}>
              Volver
            </button>
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Enviando…" : "Radicar solicitud"}
            </button>
          </div>
        </form>
      ) : null}

      {paso === 3 ? (
        <section className="tarjeta">
          {tramite ? (
            <>
              <p className="folio">{tramite.folio}</p>
              <BotonCopiar texto={tramite.folio} />
              <p>
                Estado: <span className="estado">{tramite.estado}</span>
              </p>
              <LineaTiempoVista items={tramite.lineaTiempo} />
            </>
          ) : null}
          <form onSubmit={(evento) => void consultarFolio(evento)}>
            <label htmlFor="folio">Folio del trámite</label>
            <input
              id="folio"
              name="folio"
              placeholder="TRM-2026-000001"
              value={folioConsulta}
              aria-invalid={campoInvalido(errorPago, "folio")}
              onChange={(e) => setFolioConsulta(e.target.value)}
            />
            <AvisoError error={errorPago} />
            <div className="fila-acciones">
              <button type="button" className="secundario" onClick={() => setPaso(2)}>
                Volver
              </button>
              <button type="submit" disabled={ocupado}>
                {ocupado ? "Consultando…" : "Consultar folio"}
              </button>
            </div>
          </form>
          {tramite?.estado === "APROBADO_PENDIENTE_PAGO" && liquidacion ? (
            <FormularioCobro
              liquidacion={liquidacion}
              onAutorizado={(resultado) => {
                setCobro(resultado);
                setPaso(4);
              }}
              onExpirada={(cuerpo) => {
                setLiquidacion(null);
                setErrorPago(cuerpo);
              }}
            />
          ) : null}
          {tramite && tramite.estado !== "APROBADO_PENDIENTE_PAGO" ? (
            <p>Cuando el estado sea APROBADO_PENDIENTE_PAGO y exista una liquidación, podrás cobrarla aquí.</p>
          ) : null}
        </section>
      ) : null}

      {paso === 4 && cobro ? (
        <section className="tarjeta">
          <AcusePago cobro={cobro} />
        </section>
      ) : null}
    </Pantalla>
  );
}
