INSERT INTO posts (slug, titulo, categoria, extracto, imagen, alt, fecha, contenido, publicado) VALUES
('cuidados-invierno-mascotas',
 'Cuidados de invierno para tu mascota',
 'Consejos',
 'Recomendaciones prǭcticas para proteger a perros y gatos durante los meses mǭs fr��os.',
 '/images/blog/cuidados-invierno.jpg',
 'Perro abrigado en invierno',
 '2026-05-10',
 JSON_ARRAY(
   JSON_OBJECT('type','paragraph','text','Durante el invierno, las mascotas pueden ser mǭs sensibles a los cambios de temperatura. Es importante brindarles un espacio cǭlido y protegido de corrientes de aire.'),
   JSON_OBJECT('type','paragraph','text','Ajusta su alimentaci��n segǧn su nivel de actividad, mantǸn al d��a sus controles veterinarios y evita ba��os con agua fr��a.')
 ),
 TRUE),
('importancia-vacunacion',
 'La importancia de la vacunaci��n en perros y gatos',
 'Salud',
 'Vacunar a tu mascota a tiempo previene enfermedades graves y mejora su calidad de vida.',
 '/images/blog/vacunacion.jpg',
 'Veterinario vacunando a un cachorro',
 '2026-04-22',
 JSON_ARRAY(
   JSON_OBJECT('type','paragraph','text','La vacunaci��n es una de las medidas preventivas mǭs efectivas para proteger a perros y gatos de enfermedades infecciosas.'),
   JSON_OBJECT('type','paragraph','text','Consulta con tu veterinario el calendario de vacunas segǧn la edad, especie y estilo de vida de tu mascota.')
 ),
 TRUE),
('alimentacion-saludable-mascotas',
 'Claves para una alimentaci��n saludable en mascotas',
 'Nutrici��n',
 'Consejos para elegir una dieta equilibrada que favorezca el bienestar de tu compa��ero.',
 '/images/blog/alimentacion-saludable.jpg',
 'Plato de alimento balanceado para mascotas',
 '2026-03-15',
 JSON_ARRAY(
   JSON_OBJECT('type','paragraph','text','Una alimentaci��n equilibrada contribuye al desarrollo, la energ��a y la salud general de tu mascota.'),
   JSON_OBJECT('type','paragraph','text','Elige alimentos de calidad adecuados a su etapa de vida y controla las porciones para prevenir el sobrepeso.')
 ),
 TRUE);
