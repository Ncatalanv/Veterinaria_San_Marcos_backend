INSERT INTO usuarios (run, email, password_hash, nombre, apellido, activo, rol_id)
VALUES (
  '11222333-2',
  'admin@duocuc.cl',
  '$2b$12$zAR9Mp9Y7.rkGYK5KbxcOuoiOa73fRfOOoneUzt.scxUixPZ8XL6S',
  'Admin',
  'San Marcos',
  TRUE,
  (SELECT id FROM roles WHERE nombre = 'ADMIN')
)
ON DUPLICATE KEY UPDATE
  password_hash = '$2b$12$zAR9Mp9Y7.rkGYK5KbxcOuoiOa73fRfOOoneUzt.scxUixPZ8XL6S';
