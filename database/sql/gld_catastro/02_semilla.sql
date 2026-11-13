INSERT INTO catastro.predios (id, tenant_id, clave_catastral, propietario, direccion, superficie_m2)
VALUES (
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
    '05021', '05-021-0147-003', 'María López Hernández',
    'Av. Reforma 120, Centro', 180.00
);

INSERT INTO catastro.periodos_predial (id, predio_id, periodo, importe_base, recargo_mora, fecha_vencimiento, pagado)
VALUES
(
    'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
    '2025-6', 1500.00, 180.00, CURRENT_DATE - 60, false
),
(
    'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
    '2026-1', 1500.00, 0.00, CURRENT_DATE + 20, false
);

INSERT INTO catastro.contratos_agua (
    id, tenant_id, numero_contrato, titular, direccion, periodo,
    lectura_anterior, lectura_actual, tarifa_m3, cuota_fija, pagado
) VALUES (
    'cccccccc-cccc-4ccc-8ccc-ccccccccccc1',
    '05021', 'CTA-1001', 'María López Hernández', 'Av. Reforma 120, Centro',
    '2026-2', 100, 128, 18.50, 45.00, false
);
