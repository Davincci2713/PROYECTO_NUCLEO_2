"use client";

import Link from "next/link";
import { useEffect, useState, type ReactNode } from "react";
import { leerUsuario } from "@/lib/sesion";

function Tarjeta({
  href,
  hu,
  titulo,
  texto,
  icono,
}: {
  href: string;
  hu: string;
  titulo: string;
  texto: string;
  icono: ReactNode;
}) {
  return (
    <Link className="tcard" href={href}>
      <span className="spread">
        <span className="ic" aria-hidden="true">
          {icono}
        </span>
        <span className="hu">{hu}</span>
      </span>
      <h3>{titulo}</h3>
      <p>{texto}</p>
      <span className="go">Abrir →</span>
    </Link>
  );
}

export default function InicioVentanilla() {
  const [nombre, setNombre] = useState("");

  useEffect(() => {
    setNombre(leerUsuario()?.nombre ?? "");
  }, []);

  return (
    <main id="contenido" className="contenedor">
      <p className="eyebrow">Ventanilla única</p>
      <h1>{nombre ? `Buen día, ${nombre}` : "Buen día"}</h1>
      <p className="sub">Seleccione el trámite. El municipio visible es San Rafael.</p>
      <div className="aviso a-ochre">
        <b>Aviso</b>
        <span>Si tiene predial pendiente, consúltelo con su clave catastral. Ejemplo: 05-021-0147-003.</span>
      </div>

      <div className="spread seccion">
        <h2>Ventanilla única</h2>
        <span className="tag t-blue">Épica 1 · HU-01 a HU-06</span>
      </div>
      <div className="tramites">
        <Tarjeta href="/app/predial" hu="HU-01" titulo="Impuesto predial" texto="Consulte el adeudo de su predio y páguelo en línea." icono={<Casa />} />
        <Tarjeta href="/app/agua" hu="HU-02" titulo="Agua y alcantarillado" texto="Revise su consumo y pague el recibo de agua." icono={<Gota />} />
        <Tarjeta href="/app/licencias" hu="HU-03" titulo="Licencia de funcionamiento" texto="Solicite y pague la licencia de su negocio." icono={<Local />} />
        <Tarjeta href="/app/multas" hu="HU-04" titulo="Multas de tránsito" texto="Consulte por placa y pague con descuento." icono={<Auto />} />
        <Tarjeta href="/app/construccion" hu="HU-05" titulo="Licencia de construcción" texto="Radique su obra con los documentos del predio." icono={<Obra />} />
        <Tarjeta href="/app/seguimiento" hu="HU-06" titulo="Seguimiento por folio" texto="Vea en qué va cada trámite que radicó." icono={<Lupa />} />
      </div>

      <div className="spread seccion">
        <h2>Ciudad digital</h2>
        <span className="tag t-green">Épica 3 · HU-18 a HU-25</span>
      </div>
      <div className="tramites">
        <Tarjeta href="/app/perfil" hu="HU-18" titulo="Mi perfil" texto="Sus datos y la verificación en dos pasos." icono={<Persona />} />
        <Tarjeta href="/app/ciudad/foros" hu="HU-19" titulo="Foros" texto="Pregunte y lea la respuesta del municipio." icono={<Foro />} />
        <Tarjeta href="/app/ciudad/encuestas" hu="HU-20" titulo="Encuestas" texto="Vote en las consultas abiertas." icono={<Barras />} />
        <Tarjeta href="/app/ciudad/directorio" hu="HU-21" titulo="Directorio PyME" texto="Negocios locales y registro del suyo." icono={<Bolsa />} />
        <Tarjeta href="/app/ciudad/chat" hu="HU-22" titulo="Salas de chat" texto="Converse con vecinos por tema." icono={<Chat />} />
        <Tarjeta href="/app/ciudad/alertas" hu="HU-23" titulo="Alertas" texto="Avisos y suscripción al teléfono." icono={<Campana />} />
        <Tarjeta href="/app/incidencias/nueva" hu="HU-24" titulo="Reportar un problema" texto="Alumbrado, baches o fugas, con el mapa." icono={<Pin />} />
        <Tarjeta href="/app/ciudad/eventos" hu="HU-25" titulo="Agenda de eventos" texto="Actividades del municipio e inscripción." icono={<Calendario />} />
      </div>
    </main>
  );
}

function Trazo({ children }: { children: ReactNode }) {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {children}
    </svg>
  );
}

function Casa() {
  return (
    <Trazo>
      <path d="M4 11 12 4l8 7v9H4Z M10 20v-6h4v6" />
    </Trazo>
  );
}
function Gota() {
  return (
    <Trazo>
      <path d="M12 3c4 5 6 7.6 6 10a6 6 0 0 1-12 0c0-2.4 2-5 6-10Z" />
    </Trazo>
  );
}
function Local() {
  return (
    <Trazo>
      <path d="M4 9h16l-1-4H5Z M5 9v11h14V9 M9 20v-6h6v6" />
    </Trazo>
  );
}
function Auto() {
  return (
    <Trazo>
      <path d="M4 13l2-6h12l2 6v5H4Z M7 18v2 M17 18v2 M4 13h16" />
    </Trazo>
  );
}
function Obra() {
  return (
    <Trazo>
      <path d="M3 21h18 M5 21V9l7-5 7 5v12 M9 21v-5h6v5" />
    </Trazo>
  );
}
function Lupa() {
  return (
    <Trazo>
      <path d="M11 5a6 6 0 1 0 0 12 6 6 0 0 0 0-12Z M15.5 15.5 20 20" />
    </Trazo>
  );
}
function Persona() {
  return (
    <Trazo>
      <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z M4 20c1.5-4 4.5-6 8-6s6.5 2 8 6" />
    </Trazo>
  );
}
function Foro() {
  return (
    <Trazo>
      <path d="M4 5h16v11H9l-5 4Z" />
    </Trazo>
  );
}
function Barras() {
  return (
    <Trazo>
      <path d="M5 20V10 M12 20V4 M19 20v-7" />
    </Trazo>
  );
}
function Bolsa() {
  return (
    <Trazo>
      <path d="M4 7h16 M6 7V4h12v3 M5 7l1 13h12l1-13" />
    </Trazo>
  );
}
function Chat() {
  return (
    <Trazo>
      <path d="M3 6h12v8H7l-4 3Z M9 17h8l4 3v-9h-3" />
    </Trazo>
  );
}
function Campana() {
  return (
    <Trazo>
      <path d="M6 16v-5a6 6 0 0 1 12 0v5l2 2H4Z M10 20a2 2 0 0 0 4 0" />
    </Trazo>
  );
}
function Pin() {
  return (
    <Trazo>
      <path d="M12 3c3.3 0 6 2.6 6 5.9C18 13.3 12 19 12 19S6 13.3 6 8.9C6 5.6 8.7 3 12 3Z M12 7a2 2 0 1 0 0 4 2 2 0 0 0 0-4Z" />
    </Trazo>
  );
}
function Calendario() {
  return (
    <Trazo>
      <path d="M4 6h16v14H4Z M4 10h16 M8 3v5 M16 3v5" />
    </Trazo>
  );
}
