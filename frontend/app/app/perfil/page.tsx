"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import type { Perfil, TotpActivacion } from "@/lib/tipos";

export default function PaginaPerfil() {
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [biografia, setBiografia] = useState("");
  const [colonia, setColonia] = useState("");
  const [totp, setTotp] = useState<TotpActivacion | null>(null);
  const [codigo, setCodigo] = useState("");
  const [aviso, setAviso] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [errorTotp, setErrorTotp] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  useEffect(() => {
    let cancelado = false;
    api<Perfil>("/api/v1/ciudad/perfiles/me")
      .then((datos) => {
        if (cancelado) return;
        setPerfil(datos);
        setBiografia(datos.biografia ?? "");
        setColonia(datos.colonia ?? "");
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  async function guardar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setAviso("");
    setOcupado(true);
    try {
      const datos = await api<Perfil>("/api/v1/ciudad/perfiles/me", {
        method: "PUT",
        body: { biografia, colonia },
      });
      setPerfil(datos);
      setAviso("Perfil guardado.");
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function activar() {
    setErrorTotp(null);
    setOcupado(true);
    try {
      const datos = await api<TotpActivacion>("/api/v1/auth/2fa/activar", { method: "POST" });
      setTotp(datos);
    } catch (err) {
      setErrorTotp(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function confirmar(evento: FormEvent) {
    evento.preventDefault();
    setErrorTotp(null);
    setOcupado(true);
    try {
      await api<void>("/api/v1/auth/2fa/confirmar", { method: "POST", body: { codigo } });
      setPerfil((actual) => (actual ? { ...actual, totpActivo: true } : actual));
      setAviso("Segundo factor activado. El próximo ingreso pedirá el código.");
      setTotp(null);
      setCodigo("");
    } catch (err) {
      setErrorTotp(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-18" titulo="Perfil">
      {perfil ? (
        <p>
          {perfil.nombre}. Segundo factor: {perfil.totpActivo ? "activo" : "inactivo"}.
        </p>
      ) : null}
      <form className="tarjeta" onSubmit={(evento) => void guardar(evento)}>
        <h2>Datos de ciudad</h2>
        <label htmlFor="biografia">Biografía</label>
        <textarea id="biografia" name="biografia" value={biografia} onChange={(e) => setBiografia(e.target.value)} />
        <label htmlFor="colonia">Colonia</label>
        <input id="colonia" name="colonia" value={colonia} onChange={(e) => setColonia(e.target.value)} />
        <AvisoError error={error} />
        {aviso ? <p className="ok">{aviso}</p> : null}
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            Guardar perfil
          </button>
        </div>
      </form>
      <section className="tarjeta">
        <h2>Segundo factor (TOTP)</h2>
        <p>Activa el código de un solo uso. Guarda el secreto en tu aplicación de autenticación y confirma con un código vigente.</p>
        <button type="button" onClick={() => void activar()} disabled={ocupado}>
          Activar TOTP
        </button>
        {totp ? (
          <form onSubmit={(evento) => void confirmar(evento)}>
            <span className="etiqueta">Secreto</span>
            <p className="folio">{totp.secreto}</p>
            <BotonCopiar texto={totp.secreto} etiqueta="Copiar secreto" />
            <span className="etiqueta">URL otpauth</span>
            <p>
              <code>{totp.otpauthUrl}</code>
            </p>
            <BotonCopiar texto={totp.otpauthUrl} etiqueta="Copiar URL" />
            <label htmlFor="codigo">Código de confirmación</label>
            <input
              id="codigo"
              name="codigo"
              inputMode="numeric"
              required
              placeholder="123456"
              value={codigo}
              aria-invalid={campoInvalido(errorTotp, "codigo")}
              onChange={(e) => setCodigo(e.target.value)}
            />
            <div className="fila-acciones">
              <button type="submit" disabled={ocupado}>
                Confirmar código
              </button>
            </div>
          </form>
        ) : null}
        <AvisoError error={errorTotp} />
      </section>
    </Pantalla>
  );
}
