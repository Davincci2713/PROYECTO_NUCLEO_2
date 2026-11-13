export type Usuario = {
  id: string;
  nombre: string;
  correo: string;
  rol: string;
  tenantId: string;
};

export type LoginRespuesta = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  requiere2fa: boolean;
  usuario?: Usuario;
};

export type RegistroRespuesta = {
  id: string;
  correo: string;
  codigoVerificacion: string;
};

export type Perfil = {
  usuarioId: string;
  nombre: string;
  biografia: string;
  colonia: string;
  totpActivo: boolean;
};

export type TotpActivacion = {
  secreto: string;
  otpauthUrl: string;
};

export type PeriodoPredial = {
  periodo: string;
  importeBase: number;
  recargoMora: number;
  importePeriodo: number;
  fechaVencimiento: string;
  vencido: boolean;
  pagado: boolean;
};

export type Predio = {
  claveCatastral: string;
  propietario: string;
  direccion: string;
  superficieM2: number;
  periodos: PeriodoPredial[];
};

export type Desglose = {
  periodo?: string;
  importeBase?: number;
  recargoMora?: number;
  importe?: number;
};

export type Liquidacion = {
  id: string;
  tipo: string;
  referencia?: string;
  concepto: string;
  estado: string;
  subtotal: number;
  recargos: number;
  descuento: number;
  descuentoPorcentaje: number;
  total: number;
  venceEn?: string;
  desglose?: Desglose[];
};

export type CobroRespuesta = {
  estadoPago: string;
  liquidacionId: string;
  estadoLiquidacion: string;
  folio?: string;
  transaccionId?: string;
  documentoId?: string;
  concepto: string;
  total: number;
  medio?: string;
  motivo?: string;
};

export type ContratoAgua = {
  numeroContrato: string;
  titular: string;
  direccion: string;
  periodo: string;
  lecturaAnterior: number;
  lecturaActual: number;
  consumoM3: number;
  tarifaM3: number;
  cuotaFija: number;
  importe: number;
  pagado: boolean;
};

export type Giro = {
  giro: string;
  tarifaBase: number;
  factorM2: number;
};

export type Cotizacion = {
  giro: string;
  tarifaBase: number;
  factorM2: number;
  superficieM2: number;
  total: number;
};

export type LineaTiempo = {
  estado: string;
  nota: string;
  actor: string;
  creadoEn: string;
};

export type Tramite = {
  folio: string;
  tipo: string;
  estado: string;
  titulo: string;
  ciudadanoId: string;
  liquidacionId: string | null;
  creadoEn: string;
  payload?: Record<string, unknown>;
  lineaTiempo: LineaTiempo[];
};

export type Multa = {
  folio: string;
  placa: string;
  descripcion: string;
  importe: number;
  fechaInfraccion: string;
  dentroDeProntoPago: boolean;
  descuentoProntoPago: number;
  totalAPagar: number;
  pagada: boolean;
};

export type Documento = {
  id: string;
  folio: string;
  tipo: string;
  transaccionId: string;
  titular: string;
  concepto: string;
  monto: number;
  sello: string;
  qrPayload: string;
  emitidoEn: string;
};

export type VerificacionDocumento = {
  valido: boolean;
  folio: string;
  concepto?: string;
  monto?: number;
  titular?: string;
  emitidoEn?: string;
  sello?: string;
};

export type Foro = {
  id: string;
  titulo: string;
  cuerpo: string;
  autorNombre: string;
  creadoEn: string;
  respuestas?: number;
  mensajes?: MensajeForo[];
};

export type MensajeForo = {
  id: string;
  autorNombre: string;
  cuerpo: string;
  esOficial: boolean;
  creadoEn: string;
};

export type OpcionEncuesta = {
  id: string;
  texto: string;
  votos: number;
};

export type Encuesta = {
  id: string;
  pregunta: string;
  cierraEn: string;
  cerrada: boolean;
  yaVote: boolean;
  opciones: OpcionEncuesta[];
};

export type Pyme = {
  id: string;
  nombre: string;
  giro: string;
  descripcion: string;
  telefono: string;
  colonia: string;
};

export type Sala = {
  id: string;
  nombre: string;
  tema: string;
};

export type MensajeSala = {
  id: string;
  autorNombre: string;
  texto: string;
  creadoEn: string;
};

export type PreferenciasAlerta = {
  telefono: string;
  temas: string[];
};

export type EnsayoAlerta = {
  tema: string;
  destinatarios: number;
};

export type Evento = {
  id: string;
  titulo: string;
  descripcion: string;
  lugar: string;
  iniciaEn: string;
  inscrito: boolean;
};

export type Incidencia = {
  folio: string;
  tipo: string;
  estado: string;
  direccionReferencia: string;
  lat: number;
  lng: number;
  descripcion: string;
  brigada: { nombre: string; zona: string };
  creadaEn: string;
  tieneFoto?: boolean;
};

export type VistaPrevia = {
  direccionReferencia: string;
};

export type DocumentoCarga = {
  nombre: string;
  contentType: string;
  contenidoBase64: string;
};
