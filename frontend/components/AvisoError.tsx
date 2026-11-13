import type { CuerpoError } from "@/lib/api";

export function AvisoError({ error }: { error: CuerpoError | null }) {
  if (!error) return null;
  return (
    <div className="error-caja" role="alert">
      <p>
        <strong>{error.titulo}</strong>
      </p>
      <p>{error.detalle}</p>
      {error.campo ? (
        <p>
          Campo: <code>{error.campo}</code>
        </p>
      ) : null}
      {error.ejemplo ? (
        <p>
          Ejemplo: <code>{error.ejemplo}</code>
        </p>
      ) : null}
      {error.codigo ? <p>Código: {error.codigo}</p> : null}
      <p>Estado HTTP: {error.status}</p>
    </div>
  );
}

export function campoInvalido(error: CuerpoError | null, campo: string): boolean {
  return error?.campo === campo;
}
