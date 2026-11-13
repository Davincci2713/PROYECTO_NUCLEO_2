"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { BarraSimple } from "@/components/BarraSimple";
import { api, normalizarError } from "@/lib/api";
import { esServidor, guardarUsuario, leerToken, leerUsuario, limpiarSesion } from "@/lib/sesion";
import type { Usuario } from "@/lib/tipos";

export function MarcoBackoffice({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [listo, setListo] = useState(false);

  useEffect(() => {
    if (!leerToken()) {
      router.replace("/");
      return;
    }
    let cancelado = false;
    api<Usuario>("/api/v1/auth/me")
      .then((cuenta) => {
        if (cancelado) return;
        guardarUsuario(cuenta);
        if (!esServidor(cuenta.rol)) {
          router.replace("/app");
          return;
        }
        setUsuario(cuenta);
        setListo(true);
      })
      .catch((error) => {
        if (cancelado) return;
        const local = leerUsuario();
        const cuerpo = normalizarError(error);
        if (cuerpo.status === 401 || !local) {
          limpiarSesion();
          router.replace("/");
          return;
        }
        if (!esServidor(local.rol)) {
          router.replace("/app");
          return;
        }
        setUsuario(local);
        setListo(true);
      });
    return () => {
      cancelado = true;
    };
  }, [router]);

  return (
    <>
      <a className="saltar" href="#contenido">
        Saltar al contenido
      </a>
      <BarraSimple usuario={usuario} />
      {listo ? children : <p className="contenedor">Cargando sesión…</p>}
    </>
  );
}
