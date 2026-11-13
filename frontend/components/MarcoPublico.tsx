import type { ReactNode } from "react";
import { BarraSimple } from "@/components/BarraSimple";

export function MarcoPublico({ children }: { children: ReactNode }) {
  return (
    <>
      <a className="saltar" href="#contenido">
        Saltar al contenido
      </a>
      <BarraSimple />
      {children}
    </>
  );
}
