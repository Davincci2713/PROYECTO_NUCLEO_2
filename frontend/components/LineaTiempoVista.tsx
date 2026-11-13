import { fecha } from "@/lib/formato";
import type { LineaTiempo } from "@/lib/tipos";

export function LineaTiempoVista({ items }: { items: LineaTiempo[] | undefined }) {
  if (!items || items.length === 0) return <p>Sin movimientos en la línea de tiempo.</p>;
  return (
    <ol className="tl">
      {items.map((item, indice) => (
        <li key={`${item.creadoEn}-${item.estado}-${indice}`} data-ok="1">
          <span className="dot">{indice + 1}</span>
          <div>
            <h4>{item.estado}</h4>
            <p>
              {fecha(item.creadoEn)} · {item.actor}
            </p>
            <p>{item.nota}</p>
          </div>
        </li>
      ))}
    </ol>
  );
}
