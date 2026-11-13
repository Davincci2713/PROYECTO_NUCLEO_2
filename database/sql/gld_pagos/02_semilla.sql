INSERT INTO pagos.tarifas_licencia (id, tenant_id, giro, tarifa_base, factor_m2)
VALUES
('dddddddd-dddd-4ddd-8ddd-ddddddddddd1', '05021', 'ABARROTES', 800.00, 12.00),
('dddddddd-dddd-4ddd-8ddd-ddddddddddd2', '05021', 'RESTAURANTE', 1400.00, 18.00),
('dddddddd-dddd-4ddd-8ddd-ddddddddddd3', '05021', 'OFICINA', 600.00, 10.00);

INSERT INTO pagos.multas (id, tenant_id, folio, placa, descripcion, importe, fecha_infraccion, pagada)
VALUES
(
    'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeee1',
    '05021', 'MUL-2026-001', 'ABC-123', 'Exceso de velocidad en zona escolar',
    800.00, CURRENT_DATE - 3, false
),
(
    'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeee2',
    '05021', 'MUL-2025-014', 'ABC-123', 'Estacionarse en bahía prohibida',
    1200.00, CURRENT_DATE - 40, false
);
