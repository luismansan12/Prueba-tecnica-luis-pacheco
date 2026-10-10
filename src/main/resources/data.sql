-- Datos de prueba que se cargan al iniciar la aplicación (base H2 en memoria)

-- Pólizas
INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (1, 'POL-IND-0001', 'INDIVIDUAL', 'ACTIVA', 'Ana Gómez', '2026-01-01', '2027-01-01', 12, 1500000, 18000000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (2, 'POL-COL-0002', 'COLECTIVA', 'ACTIVA', 'Inmobiliaria Los Andes', '2026-03-01', '2027-03-01', 12, 2500000, 30000000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (3, 'POL-COL-0003', 'COLECTIVA', 'CANCELADA', 'Administración Torres del Parque', '2025-06-01', '2026-06-01', 12, 1800000, 21600000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (4, 'POL-IND-0004', 'INDIVIDUAL', 'CANCELADA', 'Luis Martínez', '2025-09-01', '2026-03-01', 6, 1200000, 7200000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (5, 'POL-COL-0005', 'COLECTIVA', 'RENOVADA', 'Inmobiliaria El Prado', '2026-02-01', '2027-02-01', 12, 2100000, 25200000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (6, 'POL-COL-0006', 'COLECTIVA', 'ACTIVA', 'Administración Conjunto Arrayanes', '2026-05-01', '2027-05-01', 12, 1900000, 22800000);

INSERT INTO poliza (id, numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES (555, 'POL-COL-0555', 'COLECTIVA', 'ACTIVA', 'Inmobiliaria Santa Fe', '2026-04-01', '2027-04-01', 12, 2000000, 24000000);

-- Riesgos
INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (1, 'Cra 15 # 93-20, Bogotá', 'Ana Gómez', 'Carlos Ruiz', 'ACTIVO', 1);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (2, 'Cl 10 # 43-12 Apto 501, Medellín', 'Jorge Pérez', 'María Restrepo', 'ACTIVO', 2);
INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (3, 'Cl 10 # 43-12 Apto 702, Medellín', 'Laura Díaz', 'María Restrepo', 'ACTIVO', 2);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (4, 'Av 19 # 120-30 Apto 301, Bogotá', 'Pedro Silva', 'Andrés Rojas', 'CANCELADO', 3);
INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (5, 'Av 19 # 120-30 Apto 402, Bogotá', 'Sofía Castro', 'Andrés Rojas', 'CANCELADO', 3);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (6, 'Cra 7 # 45-10, Cali', 'Luis Martínez', 'Elena Vargas', 'CANCELADO', 4);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (7, 'Cl 85 # 11-40 Apto 203, Bogotá', 'Camila Torres', 'Ricardo Mejía', 'ACTIVO', 5);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (8, 'Cra 50 # 30-15 Casa 4, Barranquilla', 'Diego Herrera', 'Patricia Gil', 'ACTIVO', 6);
INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (9, 'Cra 50 # 30-15 Casa 9, Barranquilla', 'Valentina Ríos', 'Patricia Gil', 'CANCELADO', 6);

INSERT INTO riesgo (id, direccion_inmueble, arrendatario, arrendador, estado, poliza_id)
VALUES (10, 'Cl 26 # 68-35 Apto 1104, Bogotá', 'Mateo Rincón', 'Gloria Pardo', 'ACTIVO', 555);

-- Los ids se insertaron a mano: se mueve el contador para que los nuevos registros no choquen
ALTER TABLE poliza ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE riesgo ALTER COLUMN id RESTART WITH 1000;
