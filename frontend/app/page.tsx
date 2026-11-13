"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BarraSimple } from "@/components/BarraSimple";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { guardarPreToken, guardarSesion, leerPreToken, leerToken, limpiarPreToken } from "@/lib/sesion";
import type { LoginRespuesta } from "@/lib/tipos";

const CIUDADANO = { correo: "ciudadano@gld.demo", password: "Demo123!" };
const SERVIDOR = { correo: "servidor@gld.demo", password: "Demo123!" };

export default function PaginaLogin() {
  const router = useRouter();
  const [fase, setFase] = useState<"credenciales" | "codigo">("credenciales");
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [codigo, setCodigo] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [listo, setListo] = useState(false);

  useEffect(() => {
    if (leerToken()) {
      router.replace("/app");
      return;
    }
    if (leerPreToken()) setFase("codigo");
    setListo(true);
  }, [router]);

  async function entrar(cuenta: { correo: string; password: string }) {
    setError(null);
    setOcupado(true);
    try {
      const respuesta = await api<LoginRespuesta>("/api/v1/auth/login", {
        method: "POST",
        auth: false,
        body: cuenta,
      });
      if (respuesta.requiere2fa) {
        guardarPreToken(respuesta.accessToken);
        setCorreo(cuenta.correo);
        setFase("codigo");
        return;
      }
      if (!respuesta.usuario) {
        setError({
          titulo: "Respuesta incompleta",
          status: 200,
          detalle: "El acceso no trajo los datos del usuario.",
        });
        return;
      }
      guardarSesion(respuesta.accessToken, respuesta.usuario);
      router.push("/app");
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  function ingresar(evento: FormEvent) {
    evento.preventDefault();
    void entrar({ correo, password });
  }

  async function verificar(evento: FormEvent) {
    evento.preventDefault();
    const preToken = leerPreToken();
    if (!preToken) {
      setFase("credenciales");
      setError({
        titulo: "Falta el pre-token",
        status: 401,
        detalle: "Vuelve a ingresar correo y clave para recibir un código.",
      });
      return;
    }
    setError(null);
    setOcupado(true);
    try {
      const respuesta = await api<LoginRespuesta>("/api/v1/auth/2fa/verificar", {
        method: "POST",
        token: preToken,
        body: { codigo },
      });
      if (!respuesta.usuario) {
        setError({
          titulo: "Respuesta incompleta",
          status: 200,
          detalle: "La verificación no trajo los datos del usuario.",
        });
        return;
      }
      guardarSesion(respuesta.accessToken, respuesta.usuario);
      router.push("/app");
    } catch (err) {
      const cuerpo = normalizarError(err);
      if (cuerpo.status === 401) {
        limpiarPreToken();
        setFase("credenciales");
      }
      setError(cuerpo);
    } finally {
      setOcupado(false);
    }
  }

  return (
    <>
      <a className="saltar" href="#contenido">
        Saltar al ingreso
      </a>
      <BarraSimple />
      <main id="contenido" className="contenedor" data-hu="HU-18">
        {listo ? (
          <div className="login-grid">
            <div>
              <p className="eyebrow">Gobierno local digital</p>
              <h1>Haga sus trámites municipales sin ir a la tesorería</h1>
              <p className="sub">
                Consulte y pague el predial, el agua y las multas, y radique licencias con folio. Municipio de San Rafael.
              </p>
            </div>
            <div className="card pad">
              <h2>Ingresar</h2>
              {fase === "credenciales" ? (
                <form className="grid" onSubmit={ingresar}>
                  <label className="field" htmlFor="correo">
                    Correo
                    <input
                      id="correo"
                      name="correo"
                      type="email"
                      autoComplete="username"
                      required
                      value={correo}
                      aria-invalid={campoInvalido(error, "correo")}
                      onChange={(evento) => setCorreo(evento.target.value)}
                    />
                  </label>
                  <label className="field" htmlFor="password">
                    Clave
                    <input
                      id="password"
                      name="password"
                      type="password"
                      autoComplete="current-password"
                      required
                      value={password}
                      onChange={(evento) => setPassword(evento.target.value)}
                    />
                  </label>
                  <AvisoError error={error} />
                  <button className="btn-lg btn-block" type="submit" disabled={ocupado}>
                    {ocupado ? "Ingresando…" : "Ingresar"}
                  </button>
                  <button className="btn-lg btn-block" type="button" disabled={ocupado} onClick={() => void entrar(CIUDADANO)}>
                    Entrar como ciudadano
                  </button>
                  <button className="secundario btn-lg btn-block" type="button" disabled={ocupado} onClick={() => void entrar(SERVIDOR)}>
                    Entrar como servidor público
                  </button>
                  <p className="sub">
                    <Link href="/registro">Crear cuenta</Link>
                    {" · "}
                    <Link href="/verificar">Verificar correo</Link>
                  </p>
                </form>
              ) : (
                <form className="grid" onSubmit={(evento) => void verificar(evento)}>
                  <p className="sub">La cuenta tiene un segundo factor. Escribe el código de la aplicación.</p>
                  <label className="field" htmlFor="codigo">
                    Código
                    <input
                      id="codigo"
                      name="codigo"
                      inputMode="numeric"
                      autoComplete="one-time-code"
                      required
                      value={codigo}
                      placeholder="123456"
                      aria-invalid={campoInvalido(error, "codigo")}
                      onChange={(evento) => setCodigo(evento.target.value)}
                    />
                  </label>
                  <AvisoError error={error} />
                  <button className="btn-lg btn-block" type="submit" disabled={ocupado}>
                    {ocupado ? "Verificando…" : "Verificar código"}
                  </button>
                  <button
                    className="secundario btn-block"
                    type="button"
                    onClick={() => {
                      limpiarPreToken();
                      setFase("credenciales");
                      setError(null);
                    }}
                  >
                    Volver
                  </button>
                </form>
              )}
            </div>
          </div>
        ) : (
          <p>Cargando…</p>
        )}
      </main>
    </>
  );
}
