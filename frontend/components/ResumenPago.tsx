import { dinero, fecha } from "@/lib/formato";
import type { Liquidacion } from "@/lib/tipos";

export function FranjaImporte({ concepto, importe }: { concepto: string; importe: number | null }) {
  return (
    <div className="resumen-pago">
      <span className="etiqueta">Concepto</span>
      <p>{concepto}</p>
      <span className="etiqueta">Importe</span>
      <p className="importe">{importe === null ? "—" : dinero(importe)}</p>
    </div>
  );
}

export function ResumenLiquidacion({ liquidacion }: { liquidacion: Liquidacion }) {
  return (
    <div className="resumen-pago">
      <span className="etiqueta">Concepto</span>
      <p>{liquidacion.concepto}</p>
      <span className="etiqueta">Importe</span>
      <p className="importe">{dinero(liquidacion.total)}</p>
      <dl className="datos">
        <div>
          <dt>Subtotal</dt>
          <dd>{dinero(liquidacion.subtotal)}</dd>
        </div>
        <div>
          <dt>Recargos</dt>
          <dd>{dinero(liquidacion.recargos)}</dd>
        </div>
        <div>
          <dt>Descuento</dt>
          <dd>
            {dinero(liquidacion.descuento)} ({liquidacion.descuentoPorcentaje} %)
          </dd>
        </div>
        <div>
          <dt>Estado</dt>
          <dd>{liquidacion.estado}</dd>
        </div>
        {liquidacion.venceEn ? (
          <div>
            <dt>Vence</dt>
            <dd>{fecha(liquidacion.venceEn)}</dd>
          </div>
        ) : null}
        {liquidacion.referencia ? (
          <div>
            <dt>Referencia</dt>
            <dd>{liquidacion.referencia}</dd>
          </div>
        ) : null}
      </dl>
      {liquidacion.desglose && liquidacion.desglose.length > 0 ? (
        <ul>
          {liquidacion.desglose.map((fila) => (
            <li key={`${fila.periodo ?? ""}-${fila.importe ?? ""}`}>
              {fila.periodo ? `${fila.periodo}: ` : ""}
              {fila.importe !== undefined ? dinero(fila.importe) : ""}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
