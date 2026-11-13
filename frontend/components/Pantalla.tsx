import Link from "next/link";
import type { ReactNode } from "react";

export function Pantalla({ hu, titulo, children }: { hu?: string; titulo: string; children: ReactNode }) {
  return (
    <main id="contenido" className="contenedor" data-hu={hu}>
      <Link className="volver" href="/app">
        ← Volver al inicio
      </Link>
      {hu ? <p className="eyebrow">{hu}</p> : null}
      <h1>{titulo}</h1>
      {children}
    </main>
  );
}
