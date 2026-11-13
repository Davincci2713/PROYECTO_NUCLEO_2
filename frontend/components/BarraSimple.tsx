"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { esServidor, limpiarSesion } from "@/lib/sesion";
import type { Usuario } from "@/lib/tipos";

function iniciales(nombre: string): string {
  const partes = nombre.trim().split(/\s+/).filter(Boolean);
  return partes
    .slice(0, 2)
    .map((parte) => parte[0]?.toUpperCase() ?? "")
    .join("") || "SR";
}

function activo(pathname: string, href: string): boolean {
  if (href === "/app") return pathname === "/app";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function BarraSimple({ usuario }: { usuario?: Usuario | null }) {
  const pathname = usePathname();

  function salir() {
    limpiarSesion();
    window.location.assign("/");
  }

  return (
    <>
      <header className="topbar">
        <div className="wrap topbar-inner">
          <svg className="escudo" width="34" height="38" viewBox="0 0 34 38" aria-hidden="true">
            <path
              d="M17 1.6 32 6.3v13.2c0 8.3-6.1 14.6-15 17.1C8.1 34.1 2 27.8 2 19.5V6.3Z"
              fill="#2E8C6B"
              stroke="#9CC6B4"
              strokeWidth="1.6"
            />
            <path d="M17 9.5 23.5 26h-13Z" fill="#EAF4EF" opacity=".92" />
            <circle cx="17" cy="21" r="3.4" fill="#2E8C6B" />
          </svg>
          <div className="marca">
            <b>Ventanilla San Rafael</b>
            <span>Municipio de San Rafael</span>
          </div>
          {usuario ? (
            <nav className="topnav" aria-label="Principal">
              <Link href="/app" aria-current={activo(pathname, "/app") ? "page" : undefined}>
                Inicio
              </Link>
              <Link href="/app/seguimiento" aria-current={activo(pathname, "/app/seguimiento") ? "page" : undefined}>
                Seguimiento
              </Link>
              {esServidor(usuario.rol) ? (
                <Link href="/backoffice" aria-current={activo(pathname, "/backoffice") ? "page" : undefined}>
                  BackOffice
                </Link>
              ) : null}
              <div className="whoami">
                <span className="avatar" aria-hidden="true">
                  {iniciales(usuario.nombre)}
                </span>
                <span>{usuario.nombre}</span>
                <button type="button" onClick={salir}>
                  Salir
                </button>
              </div>
            </nav>
          ) : null}
        </div>
      </header>
      <div className="cinta" />
    </>
  );
}
