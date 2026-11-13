import { leerToken, limpiarSesion } from "@/lib/sesion";

export const TENANT_ID = "05021";

export type CuerpoError = {
  titulo: string;
  status: number;
  detalle: string;
  campo?: string;
  ejemplo?: string;
  codigo?: string;
};

export class ErrorApi extends Error implements CuerpoError {
  titulo: string;
  status: number;
  detalle: string;
  campo?: string;
  ejemplo?: string;
  codigo?: string;

  constructor(cuerpo: CuerpoError) {
    super(cuerpo.detalle);
    this.name = "ErrorApi";
    this.titulo = cuerpo.titulo;
    this.status = cuerpo.status;
    this.detalle = cuerpo.detalle;
    this.campo = cuerpo.campo;
    this.ejemplo = cuerpo.ejemplo;
    this.codigo = cuerpo.codigo;
  }
}

export function baseApi(): string {
  const configurada = process.env.NEXT_PUBLIC_API_BASE;
  if (configurada && configurada.trim()) return configurada.trim().replace(/\/$/, "");
  return "http://localhost:8080";
}

function esRegistro(valor: unknown): valor is Record<string, unknown> {
  return typeof valor === "object" && valor !== null;
}

export function normalizarError(error: unknown): CuerpoError {
  if (error instanceof ErrorApi) {
    return {
      titulo: error.titulo,
      status: error.status,
      detalle: error.detalle,
      campo: error.campo,
      ejemplo: error.ejemplo,
      codigo: error.codigo,
    };
  }
  if (error instanceof Error) {
    const crudo = error.message.trim();
    const red = !crudo || /failed to fetch|networkerror|load failed/i.test(crudo);
    return {
      titulo: "No se pudo contactar al servidor",
      status: 0,
      detalle: red ? `No hubo respuesta del gateway (${baseApi()}). Vuelve a intentar.` : crudo,
    };
  }
  return {
    titulo: "Error",
    status: 0,
    detalle: "Ocurrió un error inesperado.",
  };
}

function errorDesde(status: number, data: unknown): ErrorApi {
  const cuerpo = esRegistro(data) ? data : {};
  const titulo = typeof cuerpo.titulo === "string" ? cuerpo.titulo : "Error";
  const detalle =
    typeof cuerpo.detalle === "string" && cuerpo.detalle
      ? cuerpo.detalle
      : "La solicitud no se completó.";
  const statusCuerpo = typeof cuerpo.status === "number" ? cuerpo.status : status;
  return new ErrorApi({
    titulo,
    status: statusCuerpo,
    detalle,
    campo: typeof cuerpo.campo === "string" ? cuerpo.campo : undefined,
    ejemplo: typeof cuerpo.ejemplo === "string" ? cuerpo.ejemplo : undefined,
    codigo: typeof cuerpo.codigo === "string" ? cuerpo.codigo : undefined,
  });
}

type Opciones = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  token?: string | null;
  auth?: boolean;
};

export async function api<T = unknown>(path: string, opciones: Opciones = {}): Promise<T> {
  const headers: Record<string, string> = {
    Accept: "application/json",
    "X-Tenant-ID": TENANT_ID,
  };
  if (opciones.body !== undefined) headers["Content-Type"] = "application/json";

  let bearer: string | null = null;
  if (opciones.token !== undefined) bearer = opciones.token;
  else if (opciones.auth !== false) bearer = leerToken();
  if (bearer) headers.Authorization = `Bearer ${bearer}`;

  let respuesta: Response;
  try {
    respuesta = await fetch(`${baseApi()}${path}`, {
      method: opciones.method ?? "GET",
      headers,
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
    });
  } catch {
    throw new ErrorApi({
      titulo: "No se pudo contactar al servidor",
      status: 0,
      detalle: `No hubo respuesta del gateway (${baseApi()}). Vuelve a intentar.`,
    });
  }

  if (respuesta.status === 204) return undefined as T;

  const texto = await respuesta.text();
  let data: unknown = null;
  if (texto) {
    try {
      data = JSON.parse(texto) as unknown;
    } catch {
      data = { detalle: texto };
    }
  }

  if (!respuesta.ok) {
    if (respuesta.status === 401 && opciones.auth !== false && opciones.token === undefined) {
      limpiarSesion();
    }
    throw errorDesde(respuesta.status, data);
  }

  return data as T;
}

export async function abrirPdf(id: string): Promise<void> {
  const token = leerToken();
  const respuesta = await fetch(`${baseApi()}/api/v1/documentos/${encodeURIComponent(id)}/pdf`, {
    headers: {
      Accept: "application/pdf",
      "X-Tenant-ID": TENANT_ID,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  if (!respuesta.ok) {
    const texto = await respuesta.text();
    let data: unknown = null;
    if (texto) {
      try {
        data = JSON.parse(texto) as unknown;
      } catch {
        data = { detalle: texto };
      }
    }
    throw errorDesde(respuesta.status, data);
  }
  const blob = await respuesta.blob();
  const url = URL.createObjectURL(blob);
  const ventana = window.open(url, "_blank", "noopener,noreferrer");
  if (!ventana) {
    const enlace = document.createElement("a");
    enlace.href = url;
    enlace.target = "_blank";
    enlace.rel = "noopener";
    enlace.click();
  }
  window.setTimeout(() => URL.revokeObjectURL(url), 120000);
}
