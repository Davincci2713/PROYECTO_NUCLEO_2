"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { FormEvent, Suspense, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { MarcoPublico } from "@/components/MarcoPublico";
import { api, normalizarError, type CuerpoError } from "@/lib/api";

function FormularioVerificar() {
  const params = useSearchParams();
  const [correo, setCorreo] = useState(params.get("correo") ?? "");
  const [codigo, setCodigo] = useState("");
  const [listo, setListo] = useState(false);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setListo(false);
    setOcupado(true);
    try {
      await api<void>("/api/v1/auth/verificar-correo", {
        method: "POST",
        auth: false,
        body: { correo, codigo },
      });
      setListo(true);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <form className="tarjeta" onSubmit={(evento) => void enviar(evento)}>
      <label htmlFor="correo">Correo</label>
      <input id="correo" name="correo" type="email" autoComplete="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} />
      <label htmlFor="codigo">Código de verificación</label>
      <input
        id="codigo"
        name="codigo"
        inputMode="numeric"
        required
        placeholder="482193"
        value={codigo}
        aria-invalid={campoInvalido(error, "codigo")}
        onChange={(e) => setCodigo(e.target.value)}
      />
      <AvisoError error={error} />
      {listo ? (
        <p className="ok" role="status">
          Correo verificado. Ya puedes ingresar.
        </p>
      ) : null}
      <div className="fila-acciones">
        <button type="submit" disabled={ocupado}>
          {ocupado ? "Verificando…" : "Verificar correo"}
        </button>
        <Link className="boton secundario" href="/">
          Ir al ingreso
        </Link>
      </div>
    </form>
  );
}

export default function PaginaVerificarCorreo() {
  return (
    <MarcoPublico>
      <main id="contenido" className="contenedor" data-hu="HU-18">
        <h1>Verificar correo</h1>
        <p className="intro">Usa el código de 6 dígitos que devolvió el registro.</p>
        <Suspense fallback={<p>Cargando formulario…</p>}>
          <FormularioVerificar />
        </Suspense>
      </main>
    </MarcoPublico>
  );
}
