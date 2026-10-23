# TAREAS - Veterinaria San Marcos

> Documento maestro de planificación para el backend Spring Boot y la integración del frontend React.
>
> Repositorios:
>
> - Backend: `Veterinaria_San_Marcos_backend`
> - Frontend: `Veterinaria_San_Marcos.github.io`
>
> Base de datos local: MySQL 8 en `localhost:3306`, esquema `sanmarcos_db`.
>
> Destino de despliegue: backend en AWS EC2 y frontend en GitHub Pages.

---

## 1. Objetivo

Implementar un backend Spring Boot con MySQL, API REST versionada, autenticación JWT, autorización por roles y documentación Swagger. El frontend React debe consumir exclusivamente el backend, sin usar `mockDB` en tiempo de ejecución.

Al finalizar, el sistema debe permitir:

- Registro e inicio de sesión de usuarios.
- Gestión de productos, servicios, usuarios, órdenes y solicitudes.
- Compra de productos con descuento de stock.
- Solicitud y gestión de citas veterinarias.
- Acceso restringido según el rol del usuario.
- Documentación de la API en Swagger.
- Despliegue del frontend en GitHub Pages y del backend en AWS EC2.

---

## 2. Estado actual

- [x] Base de datos `sanmarcos_db` creada.
- [x] Proyecto Spring Boot inicializado.
- [x] Frontend React funcionando con `mockDB`.
- [x] Plan de backend y frontend definido.
- [x] Esquema `V1__schema.sql` completo con las 10 tablas.
- [x] Configuración local con `application.yml` y `application-local.yml.example`.
- [ ] Backend implementado.
- [ ] Frontend conectado al backend.
- [ ] Pruebas completas.
- [ ] Despliegue en AWS EC2 y GitHub Pages.

---

## 3. Equipo y responsabilidades

| Persona | Enfoque principal | Responsabilidades |
|---|---|---|
| A - Backend/Infraestructura | Spring Boot, MySQL, seguridad, AWS | Entidades, repositorios, servicios, JWT, Swagger, despliegue EC2 |
| B - Frontend/QA | React, integración REST, pruebas | Cliente API, contextos, pantallas, rutas por rol, pruebas, GitHub Pages |
| A + B | Documentación y validación | README, Postman, pruebas E2E, revisión de PR, presentación |

> Reemplazar A y B por los nombres reales del equipo.

---

## 4. Reglas de colaboración

- [ ] Proteger la rama `main` y trabajar con ramas `feature/...` o `fix/...`.
- [ ] Usar commits claros, por ejemplo: `feat(auth): agregar login con JWT`.
- [ ] Cada cambio entra por Pull Request y requiere revisión de la otra persona.
- [ ] No subir contraseñas, secretos JWT ni archivos `.env` reales.
- [ ] Revisión breve diaria de 15 minutos para sincronizar avances y bloqueos.
- [ ] Actualizar este archivo al cerrar cada tarea.
- [ ] Mantener Swagger y README actualizados cuando cambie un endpoint.
- [ ] No mover una tarjeta a "Hecho" si no cumple los criterios de aceptación.

---

## 5. Organización en Trello

### 5.1 Recomendación general

Usar un solo tablero para backend y frontend. Trello no tiene sprints ni swimlanes nativos, así que se debe elegir una dimensión para las listas y otra para etiquetas o campos personalizados.

---

## 6. Variables de entorno

### Backend

```properties
DB_URL=jdbc:mysql://localhost:3306/sanmarcos_db?serverTimezone=America/Santiago&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=sanmarcos_app
DB_PASSWORD=definir_en_application_local_yml
JWT_SECRET=definir_secreto_de_al_menos_32_caracteres
JWT_EXPIRATION=7200
CORS_ORIGINS=http://localhost:5173,http://localhost:4173,https://ncatalanv.github.io
SERVER_PORT=8080
```

> En local, crear `application-local.yml` a partir de `application-local.yml.example`. Spring Boot no lee archivos `.env` automáticamente. En EC2 se usan variables de entorno.

### Frontend

```properties
VITE_API_URL=http://localhost:8080/api/v1
```

En GitHub Pages, `VITE_API_URL` debe apuntar al backend público con HTTPS.

---

## 7. Matriz de roles

| Rol | Permisos |
|---|---|
| `ADMIN` | Acceso total |
| `VENDEDOR` | Ver productos y detalle; ver órdenes y detalle |
| `VETERINARIO` | Ver servicios; ver y actualizar solicitudes de servicio |
| `CLIENTE` | Tienda, carrito, checkout, sus órdenes y solicitudes |

---

## 8. Fases, tarjetas y checklists

> Cada tarjeta debe tener su checklist en la sección **Checklist** de Trello.
>
> La descripción debe contener el objetivo, criterios de aceptación, dependencias y enlaces.

### Fase 0 - Preparación

#### [BE] Configurar MySQL, Flyway y variables de entorno

- [x] Verificar conexión a `sanmarcos_db` con el usuario dedicado.
- [x] Configurar `application.yml` con perfil local y variables para producción.
- [x] Agregar variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; `JWT_SECRET` se agrega en Fase 1.
- [x] Configurar Flyway y la ubicación de las migraciones.
- [x] Crear la migración inicial `V1__schema.sql` con las 10 tablas.
- [x] Crear `application-local.yml.example`.
- [x] Verificar que la aplicación arranque con `spring-boot:run`.
- [x] Verificar que Flyway aplique `V1` en MySQL.
- [ ] Documentar las variables en el README.

> `V1__schema.sql` es la fuente de verdad del esquema. Una vez ejecutado, no se edita; los cambios se realizan en `V2`, `V3`, etc.

#### [BE] Configurar CORS y Swagger base

- [ ] Configurar orígenes permitidos: `localhost:5173` y GitHub Pages.
- [ ] Permitir métodos y headers necesarios.
- [ ] Configurar Springdoc y los metadatos de la API.
- [ ] Agregar el esquema de seguridad Bearer JWT.
- [ ] Verificar `/swagger-ui/index.html` y `/v3/api-docs`.
- [ ] Documentar la URL de Swagger.

#### [BE+FE] Definir contrato API y matriz de roles

- [ ] Listar endpoints por dominio.
- [ ] Definir DTOs de entrada y salida.
- [ ] Definir códigos HTTP y formato de errores.
- [ ] Confirmar roles: `ADMIN`, `VENDEDOR`, `VETERINARIO`, `CLIENTE`.
- [ ] Confirmar permisos por endpoint.
- [ ] Guardar el contrato en el README o en Swagger.
- [ ] Revisar el contrato en conjunto.

#### [FE] Crear cliente API y `.env.example`

- [ ] Crear `src/services/api.js`.
- [ ] Leer la URL base desde `VITE_API_URL`.
- [ ] Implementar header `Authorization: Bearer`.
- [ ] Implementar parseo de errores del backend.
- [ ] Implementar manejo global de 401.
- [ ] Crear `.env.example`.
- [ ] Documentar cómo configurar el entorno local.

**Criterio de cierre de fase:** backend y frontend levantan en local; la base responde; CORS permite el origen de Vite; Swagger está disponible.

---

### Fase 1 - Autenticación

#### [BE] Modelo de usuarios, roles y mascotas

- [ ] Crear entidad `Rol`.
- [ ] Crear entidad `Usuario` con RUN y email únicos.
- [ ] Crear entidad `Mascota` con relación al usuario.
- [ ] Definir la relación usuario-rol.
- [ ] Crear repositorios.
- [ ] Alinear las entidades con `V1__schema.sql`.
- [ ] Crear una migración Flyway solo si falta un campo o una restricción.
- [ ] Verificar las restricciones e índices existentes en `V1`.
- [ ] Probar persistencia y relaciones.

#### [BE] Implementar login, registro y JWT

- [ ] Configurar `SecurityConfig` stateless.
- [ ] Configurar BCrypt.
- [ ] Crear `JwtService` con firma y expiración.
- [ ] Crear filtro JWT.
- [ ] Implementar `UserDetailsService`.
- [ ] Implementar `POST /api/v1/auth/register`.
- [ ] Implementar `POST /api/v1/auth/login`.
- [ ] Implementar `GET /api/v1/auth/me`.
- [ ] Configurar respuesta 401 sin token.
- [ ] Documentar endpoints en Swagger.

#### [BE] Manejo global de errores y validaciones

- [ ] Crear `@RestControllerAdvice`.
- [ ] Usar `ProblemDetail`.
- [ ] Agregar validaciones a los DTOs.
- [ ] Manejar 400, 401, 403, 404 y 409.
- [ ] Validar RUN y email.
- [ ] Evitar exponer stacktraces.
- [ ] Agregar pruebas de errores.

#### [BE] Tests de autenticación y seguridad

- [ ] Probar login correcto.
- [ ] Probar login incorrecto.
- [ ] Probar registro con email duplicado.
- [ ] Probar `/auth/me` sin token.
- [ ] Probar token inválido o expirado.
- [ ] Probar acceso admin con cliente.
- [ ] Verificar que Swagger sea público.
- [ ] Ejecutar `.\mvnw.cmd test`.

#### [FE] Refactorizar AuthContext, login y registro

- [ ] Reemplazar `mockLogin` por llamadas a `authApi`.
- [ ] Hacer `login` asíncrono.
- [ ] Hacer `register` asíncrono.
- [ ] Guardar token y usuario.
- [ ] Restaurar sesión con `/auth/me`.
- [ ] Actualizar `LoginPage`.
- [ ] Actualizar `RegistroPage`.
- [ ] Eliminar imports de `mockDB` en tiempo de ejecución.

#### [FE] Manejo de sesión, 401 y rutas protegidas

- [ ] Hacer que `logout` limpie token y usuario.
- [ ] Manejar 401 desde el cliente API.
- [ ] Redirigir al login cuando corresponda.
- [ ] Actualizar `ProtectedRoute`.
- [ ] Actualizar `AdminRoute` para los cuatro roles.
- [ ] Evitar parpadeo de sesión al recargar.
- [ ] Probar rutas públicas y privadas.

**Criterio de cierre de fase:** registro, login y `/auth/me` funcionan; las rutas protegidas rechazan peticiones sin token; los roles se reconocen correctamente.

---

### Fase 2 - Catálogo

#### [BE] Modelo y migraciones de catálogo

- [ ] Crear entidad `Categoria` con tipo.
- [ ] Crear entidad `Producto`.
- [ ] Crear entidad `Servicio`.
- [ ] Definir relaciones con categoría.
- [ ] Crear repositorios.
- [ ] Crear `V2__seed_catalog.sql` solo para datos iniciales; las tablas ya existen en `V1`.
- [ ] Cargar catálogo inicial.
- [ ] Probar persistencia.

#### [BE] Endpoints públicos de productos, servicios y categorías

- [ ] Implementar `GET /api/v1/products`.
- [ ] Implementar `GET /api/v1/products/{id}`.
- [ ] Implementar `GET /api/v1/services`.
- [ ] Implementar `GET /api/v1/services/{id}`.
- [ ] Implementar `GET /api/v1/categories?type=`.
- [ ] Agregar filtros por categoría, oferta y búsqueda.
- [ ] Crear DTOs sin datos internos.
- [ ] Documentar en Swagger.

#### [BE] CRUD administrativo de productos y servicios

- [ ] Implementar `POST`, `PUT` y `DELETE` de productos.
- [ ] Implementar `POST`, `PUT` y `DELETE` de servicios.
- [ ] Agregar validaciones.
- [ ] Restringir escritura a `ADMIN`.
- [ ] Validar código único.
- [ ] Manejar 404 y 409.
- [ ] Documentar en Swagger.
- [ ] Agregar pruebas.

#### [BE] Tests de catálogo y permisos

- [ ] Probar listado público.
- [ ] Probar detalle inexistente.
- [ ] Probar crear producto como cliente.
- [ ] Probar crear producto como vendedor.
- [ ] Probar crear producto como admin.
- [ ] Probar que vendedor puede listar productos.
- [ ] Probar que veterinario no puede crear servicios.
- [ ] Ejecutar tests.

#### [FE] Refactorizar tienda, categorías, ofertas y detalles

- [ ] Crear `productsApi` y `servicesApi`.
- [ ] Refactorizar `TiendaPage`.
- [ ] Refactorizar `CategoriasPage`.
- [ ] Refactorizar `OfertasPage`.
- [ ] Refactorizar `ProductoDetallePage`.
- [ ] Refactorizar `ServicioDetallePage`.
- [ ] Agregar estados de carga, error y vacío.
- [ ] Normalizar rutas de imágenes.

#### [FE] Refactorizar admin de productos y servicios

- [ ] Refactorizar `AdminProductosPage`.
- [ ] Refactorizar `AdminProductoForm`.
- [ ] Refactorizar `AdminServiciosPage`.
- [ ] Refactorizar `AdminServicioForm`.
- [ ] Conectar crear, editar y eliminar con la API.
- [ ] Ocultar acciones de escritura a roles no autorizados.
- [ ] Manejar errores del backend.
- [ ] Recargar listas después de guardar.

**Criterio de cierre de fase:** catálogo público y CRUD administrativo funcionan contra MySQL; los permisos de escritura se respetan.

---

### Fase 3 - Órdenes

#### [BE] Modelo de órdenes y detalle

- [ ] Crear entidad `Orden`.
- [ ] Crear entidad `OrdenDetalle`.
- [ ] Definir estados de orden.
- [ ] Definir relaciones.
- [ ] Crear repositorios.
- [ ] Verificar las tablas `ordenes` y `orden_detalle` de `V1`; no crear tablas nuevas.
- [ ] Agregar índices solo si falta alguno.
- [ ] Probar persistencia.

#### [BE] Lógica transaccional de stock y totales

- [ ] Crear orden dentro de una transacción.
- [ ] Buscar productos en la base.
- [ ] Calcular precio con descuento.
- [ ] Validar stock suficiente.
- [ ] Descontar stock.
- [ ] Calcular total en el servidor.
- [ ] Generar número de orden único.
- [ ] Restaurar stock al cancelar.
- [ ] Agregar pruebas de lógica.

#### [BE] Endpoints de órdenes y permisos

- [ ] Implementar `POST /api/v1/orders`.
- [ ] Implementar `GET /api/v1/orders/me`.
- [ ] Implementar `GET /api/v1/orders`.
- [ ] Implementar `GET /api/v1/orders/{id}`.
- [ ] Implementar `PATCH /api/v1/orders/{id}/status`.
- [ ] Crear DTOs de orden y detalle.
- [ ] Documentar en Swagger.
- [ ] Probar permisos por rol.

#### [BE] Tests de órdenes, stock y cancelación

- [ ] Probar orden válida.
- [ ] Probar stock insuficiente.
- [ ] Probar total con descuento.
- [ ] Probar descuento de stock.
- [ ] Probar restauración al cancelar.
- [ ] Probar que cliente no vea órdenes ajenas.
- [ ] Probar que vendedor no modifique órdenes.
- [ ] Ejecutar tests.

#### [FE] Carrito y checkout contra la API

- [ ] Adaptar `CartContext` para obtener productos desde la API.
- [ ] Refactorizar `CarritoPage`.
- [ ] Refactorizar `CheckoutPage`.
- [ ] Enviar items y cantidades al backend.
- [ ] Mostrar el total calculado por el backend.
- [ ] Manejar error de stock insuficiente.
- [ ] Refactorizar `PagoExitoPage` y `PagoErrorPage`.
- [ ] Limpiar carrito solo tras una orden exitosa.

#### [FE] Historial de órdenes del cliente

- [ ] Crear `ordersApi`.
- [ ] Crear vista de historial.
- [ ] Crear vista de detalle de orden.
- [ ] Mostrar estados.
- [ ] Mostrar fechas y precios.
- [ ] Agregar estados de carga y vacío.
- [ ] Agregar la ruta al frontend.

#### [FE] Listado y detalle de órdenes para admin/vendedor

- [ ] Refactorizar `AdminOrdenesPage`.
- [ ] Crear `AdminOrdenDetallePage`.
- [ ] Mostrar cliente, items y total.
- [ ] Permitir cambio de estado solo a admin.
- [ ] Dejar al vendedor en modo solo lectura.
- [ ] Ocultar solicitudes de servicio al vendedor.
- [ ] Verificar rutas y permisos.

**Criterio de cierre de fase:** una compra descuenta stock, genera una orden y aparece en el panel según el rol; el vendedor solo puede consultar.

---

### Fase 4 - Solicitudes de servicio

#### [BE] Modelo y endpoints de solicitudes

- [ ] Crear entidad `SolicitudServicio`.
- [ ] Definir estados.
- [ ] Definir relaciones con usuario y servicio.
- [ ] Crear repositorio.
- [ ] Verificar la tabla `solicitudes_servicio` de `V1`; no crear tablas nuevas.
- [ ] Implementar `POST /api/v1/service-requests`.
- [ ] Implementar `GET /me`, `GET` admin y `GET /{id}`.
- [ ] Documentar en Swagger.

#### [BE] Actualización de citas y estados

- [ ] Implementar actualización de fecha y hora.
- [ ] Implementar actualización de comentario.
- [ ] Implementar cambio de estado.
- [ ] Validar transiciones permitidas.
- [ ] Permitir cancelación del cliente solo si está pendiente.
- [ ] Restringir actualización a admin y veterinario.
- [ ] Responder 409 ante estado inválido.
- [ ] Agregar pruebas.

#### [BE] Tests de solicitudes y roles

- [ ] Probar creación por cliente.
- [ ] Probar que cliente vea solo sus solicitudes.
- [ ] Probar que vendedor reciba 403.
- [ ] Probar listado y actualización por veterinario.
- [ ] Probar listado y actualización por admin.
- [ ] Probar cancelación por cliente.
- [ ] Ejecutar tests.

#### [FE] Mis solicitudes y creación desde servicios

- [ ] Crear `requestsApi`.
- [ ] Reemplazar `RequestContext` o conectarlo a la API.
- [ ] Permitir crear solicitud desde un servicio.
- [ ] Refactorizar `MiSolicitudPage`.
- [ ] Implementar cancelación.
- [ ] Mostrar estados, fechas y comentarios.
- [ ] Agregar estados de carga y vacío.

#### [FE] Panel de solicitudes para admin/veterinario

- [ ] Crear `AdminSolicitudesPage`.
- [ ] Crear vista de detalle.
- [ ] Permitir actualizar fecha y hora.
- [ ] Permitir cambiar estado.
- [ ] Agregar filtros por estado.
- [ ] Ocultar la sección al vendedor.
- [ ] Verificar rutas y permisos.

**Criterio de cierre de fase:** el cliente crea y cancela solicitudes; veterinario y admin las gestionan; el vendedor no accede a este módulo.

---

### Fase 5 - Usuarios, blog y dashboard

#### [BE] CRUD de usuarios solo admin

- [ ] Implementar `GET /api/v1/users`.
- [ ] Implementar `GET /api/v1/users/{id}`.
- [ ] Implementar `POST /api/v1/users`.
- [ ] Implementar `PUT /api/v1/users/{id}`.
- [ ] Implementar `DELETE /api/v1/users/{id}`.
- [ ] Permitir asignar roles solo a admin.
- [ ] No exponer el hash de contraseña.
- [ ] Validar email y RUN duplicados.
- [ ] Agregar pruebas.

#### [BE] Endpoints de blog

- [ ] Crear entidad `Post`.
- [ ] Verificar la tabla `posts` de `V1`.
- [ ] Crear una migración solo para los datos iniciales del blog.
- [ ] Implementar `GET /api/v1/posts`.
- [ ] Implementar `GET /api/v1/posts/{slug}`.
- [ ] Implementar CRUD solo admin.
- [ ] Definir formato del contenido.
- [ ] Documentar en Swagger.
- [ ] Agregar pruebas.

#### [BE] Endpoint de métricas del dashboard

- [ ] Contar órdenes.
- [ ] Contar solicitudes.
- [ ] Contar usuarios.
- [ ] Contar productos.
- [ ] Listar productos con stock crítico.
- [ ] Obtener últimas órdenes y solicitudes.
- [ ] Restringir a `ADMIN`.
- [ ] Agregar pruebas.

#### [FE] Gestión de usuarios

- [ ] Crear `usersApi`.
- [ ] Refactorizar `AdminUsuariosPage`.
- [ ] Refactorizar `AdminUsuarioForm`.
- [ ] Mostrar y editar roles.
- [ ] Conectar crear, editar y eliminar.
- [ ] Manejar errores del backend.
- [ ] Restringir la vista a admin.

#### [FE] Blog público y detalle

- [ ] Crear `postsApi`.
- [ ] Refactorizar `BlogPage`.
- [ ] Refactorizar `BlogDetallePage`.
- [ ] Agregar estados de carga y error.
- [ ] Renderizar el contenido del backend.
- [ ] Verificar imágenes y rutas.

#### [FE] Dashboard real y sidebar por rol

- [ ] Conectar `AdminDashboard` con métricas reales.
- [ ] Hacer dinámico `AdminSidebar`.
- [ ] Admin: todas las secciones.
- [ ] Vendedor: Productos y Órdenes.
- [ ] Veterinario: Servicios y Solicitudes.
- [ ] Cliente: sin acceso al panel admin.
- [ ] Proteger rutas por rol.
- [ ] Probar cada rol.

**Criterio de cierre de fase:** cada rol ve únicamente las secciones permitidas y el dashboard usa datos reales.

---

### Fase 6 - QA

#### [QA] Ejecutar tests backend y corregir fallos

- [ ] Ejecutar `.\mvnw.cmd test`.
- [ ] Revisar fallos.
- [ ] Corregir código o pruebas.
- [ ] Repetir hasta que pasen.
- [ ] Verificar migraciones desde una base vacía.
- [ ] Probar Swagger con un token real.

#### [QA] Ejecutar lint, tests y build frontend

- [ ] Ejecutar `npm run lint`.
- [ ] Ejecutar `npm run test:run`.
- [ ] Ejecutar `npm run build`.
- [ ] Corregir errores.
- [ ] Verificar que no queden imports de `mockDB` en runtime.

#### [QA] Prueba E2E de los cuatro roles

- [ ] Registrar e iniciar sesión como cliente.
- [ ] Iniciar sesión como admin, vendedor y veterinario.
- [ ] Probar catálogo, carrito y checkout.
- [ ] Verificar descuento de stock.
- [ ] Crear y gestionar una solicitud.
- [ ] Verificar restricciones por rol.
- [ ] Verificar respuestas 401 y 403.

#### [QA] Corregir bugs encontrados

- [ ] Registrar cada bug como tarjeta.
- [ ] Priorizar bugs bloqueantes.
- [ ] Corregir.
- [ ] Volver a probar.
- [ ] Cerrar los bugs corregidos.

**Criterio de cierre de fase:** backend y frontend compilan, las pruebas pasan y la matriz de roles se cumple en una prueba manual completa.

---

### Fase 7 - Despliegue

#### [BE] Desplegar backend en EC2

- [ ] Crear instancia EC2.
- [ ] Instalar Java 21 o superior.
- [ ] Configurar MySQL o RDS.
- [ ] Configurar variables de entorno.
- [ ] Compilar el `.jar`.
- [ ] Copiar el `.jar` a EC2.
- [ ] Crear servicio `systemd`.
- [ ] Configurar Security Group.
- [ ] Probar la API desde internet.

#### [BE] Configurar Nginx, HTTPS y CORS

- [ ] Configurar Nginx como reverse proxy.
- [ ] Configurar dominio.
- [ ] Instalar certificado HTTPS.
- [ ] Configurar CORS con el dominio de GitHub Pages.
- [ ] Verificar Swagger público.
- [ ] Reiniciar servicios.
- [ ] Probar desde internet.

#### [FE] Configurar GitHub Pages y VITE_API_URL

- [ ] Configurar `VITE_API_URL` con HTTPS.
- [ ] Verificar `base` de Vite.
- [ ] Configurar GitHub Actions.
- [ ] Configurar secretos del repositorio.
- [ ] Ejecutar deploy.
- [ ] Probar login y catálogo.
- [ ] Verificar que no haya contenido mixto.

#### [QA] Verificar frontend público contra AWS

- [ ] Probar registro y login.
- [ ] Probar catálogo.
- [ ] Probar checkout.
- [ ] Probar panel admin.
- [ ] Probar cada rol.
- [ ] Revisar errores de CORS y HTTPS.
- [ ] Documentar el resultado.

**Criterio de cierre de fase:** el frontend público consume el backend en AWS sin errores de CORS ni contenido mixto.

---

### Fase 8 - Cierre

#### [DOC] README, Postman y diagrama ER

- [ ] Documentar instalación.
- [ ] Documentar SQL de base de datos.
- [ ] Documentar variables de entorno.
- [ ] Documentar credenciales demo.
- [ ] Documentar endpoints.
- [ ] Documentar roles y permisos.
- [ ] Agregar URL de Swagger.
- [ ] Exportar colección Postman.
- [ ] Agregar diagrama ER en Mermaid.

#### [DOC] Revisión de rúbrica

- [ ] Revisar calidad del backend.
- [ ] Revisar integración REST y Swagger.
- [ ] Revisar seguridad y JWT.
- [ ] Revisar colaboración y repositorios.
- [ ] Revisar integración con frontend.
- [ ] Revisar base de datos.
- [ ] Reunir evidencias y capturas.

#### [DOC] Guion de demostración

- [ ] Definir orden de la demo.
- [ ] Preparar usuarios demo.
- [ ] Preparar flujo de cliente.
- [ ] Preparar flujo de admin.
- [ ] Preparar flujo de vendedor.
- [ ] Preparar flujo de veterinario.
- [ ] Incluir Swagger.
- [ ] Ensayar la presentación.

**Criterio de cierre de fase:** documentación completa, rúbrica cubierta y demostración ensayada.

---

## 9. Checklist de cierre común

Agregar estos ítems al final de cada tarjeta o usarlos como plantilla:

- [ ] El código compila.
- [ ] Las pruebas relacionadas pasan.
- [ ] Swagger está actualizado.
- [ ] Los permisos por rol fueron probados.
- [ ] No se subieron secretos.
- [ ] README o `TAREAS.md` fue actualizado.
- [ ] El Pull Request fue revisado.
- [ ] La tarjeta cumple los criterios de aceptación.

---

## 10. Definition of Done

Una tarea se considera terminada cuando:

- El código compila sin errores.
- Las pruebas relacionadas pasan.
- El endpoint está documentado en Swagger.
- Los permisos por rol están probados.
- No se subieron secretos.
- El README o `TAREAS.md` fue actualizado.
- Otro integrante revisó el cambio.
- La funcionalidad fue probada manualmente cuando corresponde.

---

## 11. Riesgos y decisiones

| Riesgo | Mitigación |
|---|---|
| GitHub Pages usa HTTPS y el backend HTTP | Publicar el backend con HTTPS; evitar contenido mixto |
| CORS bloquea el frontend | Configurar orígenes exactos por entorno |
| Stock negativo por compras simultáneas | Transacciones y bloqueo optimista o pesimista |
| Diferencias entre el modelo de la Figura 1 y el esquema | Validar nombres de tablas antes de la migración final |
| Pérdida de datos al reiniciar | Usar MySQL persistente; H2 solo para pruebas |
| Vendedor accede a módulos no permitidos | Restricción en backend y rutas/sidebar en frontend |
| Contraseñas o JWT expuestos | Variables de entorno y `.gitignore` |
| Frontend de GitHub Pages no alcanza `localhost` | Configurar `VITE_API_URL` con la URL pública de EC2 |

---

## 12. Comandos útiles

### Backend

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Frontend

```powershell
npm install
npm run dev
npm run lint
npm run test:run
npm run build
```

### Swagger local

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

---

## 13. Registro de avances

| Fecha | Persona | Avance | Bloqueos |
|---|---|---|---|
| | | | |
| | | | |
| | | | |
