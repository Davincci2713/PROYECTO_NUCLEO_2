"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { MarcoPublico } from "@/components/MarcoPublico";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import type { RegistroRespuesta } from "@/lib/tipos";

export default function PaginaRegistro() {
  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [telefono, setTelefono] = useState("");
  const [creado, setCreado] = useState<RegistroRespuesta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setCreado(null);
    setOcupado(true);
    try {
      const respuesta = await api<RegistroRespuesta>("/api/v1/auth/registro", {
        method: "POST",
        auth: false,
        body: { nombre, correo, password, telefono },
      });
      setCreado(respuesta);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <MarcoPublico>
      <main id="contenido" className="contenedor" data-hu="HU-18">
        <h1>Registro de ciudadano</h1>
        <p className="intro">La contraseña tiene al menos 8 caracteres. El municipio no envía el código por correo: aparece aquí para que puedas verificar la cuenta.</p>
        <form className="tarjeta" onSubmit={(evento) => void enviar(evento)}>
          <label htmlFor="nombre">Nombre</label>
          <input id="nombre" name="nombre" autoComplete="name" required value={nombre} onChange={(e) => setNombre(e.target.value)} />
          <label htmlFor="correo">Correo</label>
          <input
            id="correo"
            name="correo"
            type="email"
            autoComplete="email"
            required
            value={correo}
            aria-invalid={campoInvalido(error, "correo")}
            onChange={(e) => setCorreo(e.target.value)}
          />
          <label htmlFor="password">Clave</label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <label htmlFor="telefono">Teléfono</label>
          <input id="telefono" name="telefono" type="tel" autoComplete="tel" required value={telefono} onChange={(e) => setTelefono(e.target.value)} />
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Creando cuenta…" : "Crear cuenta"}
            </button>
          </div>
        </form>
        {creado ? (
          <div className="ok" role="status">
            <p>Cuenta creada para {creado.correo}.</p>
            <span className="etiqueta">Código de verificación</span>
            <p className="folio">{creado.codigoVerificacion}</p>
            <div className="fila-acciones">
              <BotonCopiar texto={creado.codigoVerificacion} etiqueta="Copiar código" />
              <Link className="boton" href={`/verificar?correo=${encodeURIComponent(creado.correo)}`}>
                Verificar correo
              </Link>
            </div>
          </div>
        ) : null}
      </main>
    </MarcoPublico>
  );
}
