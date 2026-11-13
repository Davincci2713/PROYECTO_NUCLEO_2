"use client";

import { useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero } from "@/lib/formato";
import type { ContratoAgua } from "@/lib/tipos";

export default function PaginaContratos() {
  const [contratos, setContratos] = useState<ContratoAgua[] | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);

  useEffect(() => {
    let cancelado = false;
    api<ContratoAgua[]>("/api/v1/agua/contratos")
      .then((lista) => {
        if (!cancelado) setContratos(lista);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  return (
    <Pantalla hu="HU-02" titulo="Contratos de agua">
      <AvisoError error={error} />
      {contratos && contratos.length === 0 ? <p>No hay contratos en el padrón.</p> : null}
      {contratos && contratos.length > 0 ? (
        <div className="tabla-contenedor">
          <table>
            <thead>
              <tr>
                <th>Contrato</th>
                <th>Titular</th>
                <th>Dirección</th>
                <th>Periodo</th>
                <th>Consumo m³</th>
                <th>Lecturas</th>
                <th>Tarifa</th>
                <th>Importe</th>
                <th>Pagado</th>
              </tr>
            </thead>
            <tbody>
              {contratos.map((contrato) => (
                <tr key={contrato.numeroContrato}>
                  <td>{contrato.numeroContrato}</td>
                  <td>{contrato.titular}</td>
                  <td>{contrato.direccion}</td>
                  <td>{contrato.periodo}</td>
                  <td>{contrato.consumoM3}</td>
                  <td>
                    {contrato.lecturaAnterior} → {contrato.lecturaActual}
                  </td>
                  <td>{dinero(contrato.tarifaM3)}</td>
                  <td>{dinero(contrato.importe)}</td>
                  <td>{contrato.pagado ? "Sí" : "No"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </Pantalla>
  );
}
