-- Las migraciones V2 y V3 se editaron despues de haber sido aplicadas en la BD
-- (commits ceb2f0a y b816d90). Flyway detecta el cambio de checksum y cancela
-- el arranque. Los cambios de datos deben hacerse en una migracion nueva,
-- nunca modificando una migracion ya aplicada.
-- Esta migracion deja las rutas de imagenes finales tanto en BDs nuevas como
-- en las que ya corrieron V1..V4 con las rutas antiguas.

UPDATE productos
   SET imagen = CASE codigo
        WHEN 'ALI-001' THEN '/images/alimento_perro.png'
        WHEN 'ALI-002' THEN '/images/comida_gato.png'
        WHEN 'MED-001' THEN '/images/antiparasitario_perro_gato.png'
        WHEN 'ACC-001' THEN '/images/correa.png'
        WHEN 'HIG-001' THEN '/images/champoo.png'
   END
 WHERE codigo IN ('ALI-001', 'ALI-002', 'MED-001', 'ACC-001', 'HIG-001');

UPDATE servicios
   SET imagen = CASE codigo
        WHEN 'CON-001' THEN '/images/consulta_emergencia.png'
        WHEN 'VAC-001' THEN '/images/vacuna.png'
        WHEN 'CIR-001' THEN '/images/cirugia.png'
        WHEN 'PEL-001' THEN '/images/corte_pelo.png'
        WHEN 'EXA-001' THEN '/images/hemograma.png'
   END
 WHERE codigo IN ('CON-001', 'VAC-001', 'CIR-001', 'PEL-001', 'EXA-001');

UPDATE posts
   SET imagen = CASE slug
        WHEN 'cuidados-invierno-mascotas'      THEN '/images/veterinaria.png'
        WHEN 'importancia-vacunacion'          THEN '/images/calendario_vacunas.png'
        WHEN 'alimentacion-saludable-mascotas' THEN '/images/alimento_perro.png'
   END
 WHERE slug IN ('cuidados-invierno-mascotas', 'importancia-vacunacion', 'alimentacion-saludable-mascotas');
