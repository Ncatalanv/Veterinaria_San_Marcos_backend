INSERT INTO categorias (nombre, tipo, descripcion) VALUES
('Alimentos',   'PRODUCTO', 'Alimentos balanceados y snacks para mascotas'),
('Medicamentos', 'PRODUCTO', 'Medicamentos y tratamientos veterinarios'),
('Accesorios',  'PRODUCTO', 'Collares, correas, juguetes y accesorios'),
('Higiene',     'PRODUCTO', 'Productos de aseo e higiene para mascotas'),
('Consultas',   'SERVICIO', 'Atención veterinaria general y diagnóstico'),
('Vacunación',  'SERVICIO', 'Plan de vacunación para perros y gatos'),
('Cirugía',     'SERVICIO', 'Cirugías mayores y menores'),
('Peluquería',  'SERVICIO', 'Baño, corte y estética canina/felina'),
('Exámenes',    'SERVICIO', 'Laboratorio clínico y diagnóstico por imágenes');

-- Categorías productos
SET @cat_alimentos   = (SELECT id FROM categorias WHERE nombre = 'Alimentos'   AND tipo = 'PRODUCTO');
SET @cat_medicamentos = (SELECT id FROM categorias WHERE nombre = 'Medicamentos' AND tipo = 'PRODUCTO');
SET @cat_accesorios  = (SELECT id FROM categorias WHERE nombre = 'Accesorios'  AND tipo = 'PRODUCTO');
SET @cat_higiene     = (SELECT id FROM categorias WHERE nombre = 'Higiene'     AND tipo = 'PRODUCTO');

-- Categorías d servicios
SET @cat_consultas   = (SELECT id FROM categorias WHERE nombre = 'Consultas'  AND tipo = 'SERVICIO');
SET @cat_vacunacion  = (SELECT id FROM categorias WHERE nombre = 'Vacunación' AND tipo = 'SERVICIO');
SET @cat_cirugia     = (SELECT id FROM categorias WHERE nombre = 'Cirugía'    AND tipo = 'SERVICIO');
SET @cat_peluqueria  = (SELECT id FROM categorias WHERE nombre = 'Peluquería' AND tipo = 'SERVICIO');
SET @cat_examenes    = (SELECT id FROM categorias WHERE nombre = 'Exámenes'   AND tipo = 'SERVICIO');

INSERT INTO productos (codigo, nombre, descripcion, presentacion, precio, descuento, stock, stock_critico, imagen, categoria_id) VALUES
('ALI-001', 'Croquetas Premium Perro', 'Alimento balanceado para perros adultos', 'Bolsa 10 kg', 45990, 10, 50, 10, '/images/alimento_perro.png', @cat_alimentos),
('ALI-002', 'Croquetas Premium Gato', 'Alimento balanceado para gatos adultos', 'Bolsa 5 kg', 32990, 0, 30, 8, '/images/comida_gato.png', @cat_alimentos),
('MED-001', 'Antiparasitario Interno', 'Tabletas desparasitantes para perros y gatos', 'Caja 4 tabletas', 12500, 0, 100, 20, '/images/antiparasitario_perro_gato.png', @cat_medicamentos),
('ACC-001', 'Correa Reflectante', 'Correa de nylon reflectante, talla M', 'Unidad', 8990, 0, 75, 15, '/images/correa.png', @cat_accesorios),
('HIG-001', 'Shampoo Hipoalergénico', 'Shampoo suave para pieles sensibles', 'Botella 250 ml', 11500, 0, 40, 10, '/images/champoo.png', @cat_higiene);

INSERT INTO servicios (codigo, nombre, descripcion, especie, duracion, precio, imagen, nota, categoria_id) VALUES
('CON-001', 'Consulta Veterinaria General', 'Revisión completa y diagnóstico inicial', 'Perro/Gato', '30 min', 25000, '/images/consulta_emergencia.png', 'Incluye control de peso y temperatura', @cat_consultas),
('VAC-001', 'Vacunación', 'Aplicación de vacunas del plan anual', 'Perro/Gato', '15 min', 18000, '/images/vacuna.png', 'Traer cartilla de vacunación', @cat_vacunacion),
('CIR-001', 'Cirugía Menor', 'Procedimientos quirúrgicos ambulatorios', 'Perro/Gato', '1-2 hrs', 150000, '/images/cirugia.png', 'Previa evaluación veterinaria', @cat_cirugia),
('PEL-001', 'Baño y Corte', 'Servicio de estética e higiene', 'Perro/Gato', '1 hr', 22000, '/images/corte_pelo.png', 'Confirmar disponibilidad de hora', @cat_peluqueria),
('EXA-001', 'Examen de Sangre', 'Perfil bioquímico básico', 'Perro/Gato', '30 min', 35000, '/images/hemograma.png', 'Ayuno de 8 hrs previo', @cat_examenes);
