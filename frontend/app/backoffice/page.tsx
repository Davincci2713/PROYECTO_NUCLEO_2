"use client";

import Link from "next/link";
import { Pantalla } from "@/components/Pantalla";

export default function InicioBackoffice() {
  return (
    <Pantalla titulo="Servidor">
      <ul className="menu-corto">
        <li>
          <Link href="/backoffice/bandeja">Bandeja</Link>
        </li>
        <li>
          <Link href="/backoffice/agua">Contratos de agua</Link>
        </li>
      </ul>
    </Pantalla>
  );
}
