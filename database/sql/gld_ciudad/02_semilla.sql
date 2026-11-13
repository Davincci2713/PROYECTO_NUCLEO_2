INSERT INTO ciudad.perfiles (usuario_id, tenant_id, nombre, biografia, colonia)
VALUES (
    '11111111-1111-4111-8111-111111111111',
    '05021', 'María López Hernández', 'Vecina del centro.', 'Centro'
);

INSERT INTO ciudad.foros (id, tenant_id, autor_id, titulo, cuerpo)
VALUES (
    'f1111111-1111-4111-8111-111111111111',
    '05021', '11111111-1111-4111-8111-111111111111',
    'Alumbrado de la calle Reforma',
    'Las luminarias de Reforma llevan una semana apagadas.'
);

INSERT INTO ciudad.encuestas (id, tenant_id, pregunta, cierra_en, creador_id)
VALUES (
    'f2222222-2222-4222-8222-222222222222',
    '05021', '¿Qué obra debe priorizar el municipio este trimestre?',
    now() + interval '30 days',
    '22222222-2222-4222-8222-222222222222'
);

INSERT INTO ciudad.opciones_encuesta (id, encuesta_id, texto)
VALUES
('f3333333-3333-4333-8333-333333333331', 'f2222222-2222-4222-8222-222222222222', 'Bacheo'),
('f3333333-3333-4333-8333-333333333332', 'f2222222-2222-4222-8222-222222222222', 'Alumbrado'),
('f3333333-3333-4333-8333-333333333333', 'f2222222-2222-4222-8222-222222222222', 'Parques');

INSERT INTO ciudad.pymes (id, tenant_id, dueno_id, nombre, giro, descripcion, telefono, colonia)
VALUES (
    'f4444444-4444-4444-8444-444444444444',
    '05021', '11111111-1111-4111-8111-111111111111',
    'Tortillería El Centro', 'Alimentos', 'Tortilla y antojitos de barrio.',
    '5511112233', 'Centro'
);

INSERT INTO ciudad.salas (id, tenant_id, nombre, tema)
VALUES (
    'f5555555-5555-4555-8555-555555555555',
    '05021', 'Vecinos del centro', 'Convivencia y avisos de la colonia'
);

INSERT INTO ciudad.palabras_bloqueadas (palabra)
VALUES ('idiota'), ('estupido'), ('estúpido'), ('mierda'), ('pendejo');

INSERT INTO ciudad.preferencias_alerta (usuario_id, tenant_id, telefono, temas)
VALUES (
    '11111111-1111-4111-8111-111111111111',
    '05021', '5512340001', ARRAY['SISMO', 'SERVICIOS']
);

INSERT INTO ciudad.eventos (id, tenant_id, titulo, descripcion, lugar, inicia_en, creador_id)
VALUES (
    'f6666666-6666-4666-8666-666666666666',
    '05021', 'Cabildo abierto', 'Sesión pública de la alcaldía.',
    'Palacio municipal', now() + interval '7 days',
    '22222222-2222-4222-8222-222222222222'
);
