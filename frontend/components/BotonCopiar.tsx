"use client";

import { useEffect, useState } from "react";

export function BotonCopiar({ texto, etiqueta = "Copiar folio" }: { texto: string; etiqueta?: string }) {
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    setCopiado(false);
  }, [texto]);

  async function copiar() {
    try {
      await navigator.clipboard.writeText(texto);
    } catch {
      const campo = document.createElement("textarea");
      campo.value = texto;
      document.body.appendChild(campo);
      campo.select();
      document.execCommand("copy");
      document.body.removeChild(campo);
    }
    setCopiado(true);
  }

  return (
    <button type="button" className="secundario" onClick={() => void copiar()}>
      {copiado ? "Copiado" : etiqueta}
    </button>
  );
}
