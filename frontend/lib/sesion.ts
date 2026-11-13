import type { Usuario } from "@/lib/tipos";

const CLAVE_TOKEN = "gld.accessToken";
const CLAVE_USUARIO = "gld.usuario";
const CLAVE_PRE = "gld.preToken";

function almacenamiento(): Storage | null {
  if (typeof window === "undefined") return null;
  return window.sessionStorage;
}

export function leerToken(): string | null {
  return almacenamiento()?.getItem(CLAVE_TOKEN) ?? null;
}

export function leerPreToken(): string | null {
  return almacenamiento()?.getItem(CLAVE_PRE) ?? null;
}

export function leerUsuario(): Usuario | null {
  const crudo = almacenamiento()?.getItem(CLAVE_USUARIO);
  if (!crudo) return null;
  try {
    const valor = JSON.parse(crudo) as Usuario;
    if (!valor || typeof valor.correo !== "string" || typeof valor.rol !== "string") return null;
    return valor;
  } catch {
    return null;
  }
}

export function guardarSesion(token: string, usuario: Usuario): void {
  const caja = almacenamiento();
  if (!caja) return;
  caja.setItem(CLAVE_TOKEN, token);
  caja.setItem(CLAVE_USUARIO, JSON.stringify(usuario));
  caja.removeItem(CLAVE_PRE);
}

export function guardarUsuario(usuario: Usuario): void {
  almacenamiento()?.setItem(CLAVE_USUARIO, JSON.stringify(usuario));
}

export function guardarPreToken(token: string): void {
  const caja = almacenamiento();
  if (!caja) return;
  caja.setItem(CLAVE_PRE, token);
  caja.removeItem(CLAVE_TOKEN);
  caja.removeItem(CLAVE_USUARIO);
}

export function limpiarPreToken(): void {
  almacenamiento()?.removeItem(CLAVE_PRE);
}

export function limpiarSesion(): void {
  const caja = almacenamiento();
  if (!caja) return;
  caja.removeItem(CLAVE_TOKEN);
  caja.removeItem(CLAVE_USUARIO);
  caja.removeItem(CLAVE_PRE);
}

export function esServidor(rol: string | undefined | null): boolean {
  return rol === "SERVIDOR_PUBLICO" || rol === "ADMINISTRADOR";
}
