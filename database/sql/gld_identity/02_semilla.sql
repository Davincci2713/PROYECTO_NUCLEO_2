-- Semilla de identidad. Contraseña de las tres cuentas: Demo123!

INSERT INTO identity.usuarios (
    id, tenant_id, nombre, correo, password_hash, telefono, rol, correo_verificado
) VALUES
(
    '11111111-1111-4111-8111-111111111111',
    '05021', 'María López Hernández', 'ciudadano@gld.demo',
    '$2b$10$GUVyERwROtQO5y/b.f4QB.tm/OQFljTd9k6gKvHHIC3r.EKMzZasm',
    '5512340001', 'CIUDADANO', true
),
(
    '22222222-2222-4222-8222-222222222222',
    '05021', 'Jorge Ramírez', 'servidor@gld.demo',
    '$2b$10$GUVyERwROtQO5y/b.f4QB.tm/OQFljTd9k6gKvHHIC3r.EKMzZasm',
    '5512340002', 'SERVIDOR_PUBLICO', true
),
(
    '33333333-3333-4333-8333-333333333333',
    '05021', 'Ana Torres', 'admin@gld.demo',
    '$2b$10$GUVyERwROtQO5y/b.f4QB.tm/OQFljTd9k6gKvHHIC3r.EKMzZasm',
    '5512340003', 'ADMINISTRADOR', true
);
