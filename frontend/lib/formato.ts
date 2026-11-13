import type { DocumentoCarga } from "@/lib/tipos";

export function dinero(valor: number | null | undefined): string {
  if (valor === null || valor === undefined || Number.isNaN(valor)) return "—";
  return new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency: "MXN",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(valor);
}

export function fecha(valor?: string | null): string {
  if (!valor) return "—";
  if (/^\d{4}-\d{2}-\d{2}$/.test(valor)) {
    const [anio, mes, dia] = valor.split("-");
    return `${dia}/${mes}/${anio}`;
  }
  const date = new Date(valor);
  if (Number.isNaN(date.getTime())) return valor;
  return new Intl.DateTimeFormat("es-MX", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function redondear(valor: number): number {
  return Math.round((valor + Number.EPSILON) * 100) / 100;
}

export function leerArchivo(file: File): Promise<DocumentoCarga> {
  return new Promise((resolve, reject) => {
    const lector = new FileReader();
    lector.onerror = () => reject(lector.error ?? new Error("No se pudo leer el archivo"));
    lector.onload = () => {
      const resultado = typeof lector.result === "string" ? lector.result : "";
      const coma = resultado.indexOf(",");
      resolve({
        nombre: file.name,
        contentType: file.type || "application/octet-stream",
        contenidoBase64: coma >= 0 ? resultado.slice(coma + 1) : resultado,
      });
    };
    lector.readAsDataURL(file);
  });
}

export async function leerArchivos(lista: FileList | File[]): Promise<DocumentoCarga[]> {
  return Promise.all(Array.from(lista).map((archivo) => leerArchivo(archivo)));
}

export function parametro(valor: string | string[] | undefined): string {
  if (Array.isArray(valor)) return valor[0] ?? "";
  return valor ?? "";
}
