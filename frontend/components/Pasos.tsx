export function Pasos({ actual, pasos }: { actual: number; pasos: string[] }) {
  return (
    <ol className="steps" aria-label="Pasos del trámite">
      {pasos.map((nombre, indice) => {
        const numero = indice + 1;
        const estado = numero === actual ? "now" : numero < actual ? "done" : undefined;
        return (
          <li key={nombre} data-on={estado} aria-current={numero === actual ? "step" : undefined}>
            <b>{numero < actual ? "✓" : numero}</b>
            {nombre}
          </li>
        );
      })}
    </ol>
  );
}
