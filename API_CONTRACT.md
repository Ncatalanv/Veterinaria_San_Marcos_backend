# Contrato API - Veterinaria San Marcos

Versión: `v1`  
Base URL: `http://localhost:8080/api/v1` (local) / `https://[dominio-ec2]/api/v1` (producción)  
Autenticación: JWT Bearer Token  
Formato de respuesta: JSON  
Errores: RFC 7807 `application/problem+json`  

## 1. Matriz de Roles

| Rol | Descripción | Permisos |
|---|---|---|
| `ADMIN` | Administrador del sistema | Acceso total a todos los endpoints. |
| `VENDEDOR` | Vendedor/encargado de ventas | Ver productos, servicios, categorías. Ver y consultar órdenes (solo lectura). **No** puede modificar estados de órdenes ni acceder a solicitudes de servicio. |
| `VETERINARIO` | Veterinario | Ver servicios. Ver, actualizar fecha/hora/comentario y cambiar estado de solicitudes de servicio asignadas. **No** puede acceder a productos/órdenes/usuarios. |
| `CLIENTE` | Cliente registrado | Ver catálogo público. Gestionar carrito/checkout (crear órdenes). Ver sus propias órdenes. Crear y cancelar solicitudes de servicio (solo si están PENDIENTE). Ver su perfil. |

## 2. Convenciones

### Códigos HTTP
| Código | Uso |
|---|---|
| `200 OK` | Consulta exitosa |
| `201 Created` | Recurso creado exitosamente |
| `204 No Content` | Eliminación exitosa (o desactivación con soft delete) |
| `400 Bad Request` | Datos inválidos / validación fallida |
| `401 Unauthorized` | Token faltante, inválido o expirado |
| `403 Forbidden` | Sin permisos para el recurso |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto (duplicado, stock insuficiente, transición de estado inválida) |
| `422 Unprocessable Entity` | Error de negocio semántico (opcional) |

### Formato de Error (Problem Details)
Todos los errores se devolverán con `Content-Type: application/problem+json` siguiendo RFC 7807.

**Ejemplo 400 - Validación fallida**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Uno o más campos son inválidos",
  "instance": "/api/v1/auth/register",
  "errors": [
    { "field": "email", "message": "Debe ser un correo válido" },
    { "field": "password", "message": "La contraseña debe tener al menos 8 caracteres" }
  ]
}
```

**Ejemplo 401 - No autenticado**
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Token faltante o inválido",
  "instance": "/api/v1/auth/me"
}
```

**Ejemplo 403 - Sin permisos**
```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "No tienes permisos para acceder a este recurso",
  "instance": "/api/v1/users"
}
```

**Ejemplo 409 - Conflicto de negocio**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Stock insuficiente para el producto PROD-001",
  "instance": "/api/v1/orders"
}
```

### Paginación
Aplica a listados con muchos registros: `/users`, `/orders` (todas), `/posts` (admin), y opcionalmente a otros listados. 

```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "empty": false
}
```

### Eliminación (Soft Delete)
Los endpoints marcados como "Eliminar/desactivar" usan **soft delete**: se actualiza el campo `activo = false` (o estado equivalente) en lugar de borrar físicamente el registro. Esto garantiza la integridad referencial con `ordenes`, `orden_detalle` y `solicitudes_servicio`. El recurso desactivado no aparecerá en listados públicos, pero su histórico se mantiene.

## 3. Endpoints por Dominio

### 3.1 Autenticación (`/auth`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `POST` | `/auth/register` | No | Público | Registrar nuevo cliente. Por defecto rol `CLIENTE`. |
| `POST` | `/auth/login` | No | Público | Iniciar sesión. Retorna JWT. |
| `GET` | `/auth/me` | Sí | Cualquiera autenticado | Obtener perfil del usuario autenticado. |

#### DTOs
**RegisterRequest**
```json
{
  "run": "12345678-9",
  "email": "cliente@ejemplo.cl",
  "password": "Password123!",
  "nombre": "Juan",
  "apellido": "Pérez",
  "fechaNacimiento": "1990-01-01",
  "telefono": "+56912345678",
  "direccion": "Calle 123",
  "region": "Metropolitana",
  "comuna": "Santiago"
}
```

**LoginRequest**
```json
{
  "email": "cliente@ejemplo.cl",
  "password": "Password123!"
}
```

**AuthResponse**
```json
{
  "token": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "usuario": {
    "id": 1,
    "run": "12345678-9",
    "email": "cliente@ejemplo.cl",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "CLIENTE",
    "activo": true
  }
}
```

### 3.2 Usuarios (`/users`) - Solo ADMIN
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/users` | Sí | `ADMIN` | Listar todos los usuarios (con paginación/filtros). |
| `GET` | `/users/{id}` | Sí | `ADMIN` | Obtener usuario por ID. |
| `POST` | `/users` | Sí | `ADMIN` | Crear usuario (puede asignar cualquier rol). |
| `PUT` | `/users/{id}` | Sí | `ADMIN` | Actualizar usuario (incluye rol/activo). |
| `DELETE` | `/users/{id}` | Sí | `ADMIN` | Eliminar/desactivar usuario. |

**Nota:** Nunca exponer `password_hash`.

### 3.3 Categorías (`/categories`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/categories?type=PRODUCTO\|SERVICIO` | No | Público | Listar categorías filtradas por tipo. |

### 3.4 Productos (`/products`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/products` | No | Público | Listar productos activos. Filtros: `categoriaId`, `q` (búsqueda), `oferta=true` (con descuento). |
| `GET` | `/products/{id}` | No | Público | Detalle de producto. |
| `POST` | `/products` | Sí | `ADMIN` | Crear producto. |
| `PUT` | `/products/{id}` | Sí | `ADMIN` | Actualizar producto. |
| `DELETE` | `/products/{id}` | Sí | `ADMIN` | Eliminar/desactivar producto. |

**Nota:** `VENDEDOR` puede listar/consultar pero **NO** puede crear/editar/eliminar.

### 3.5 Servicios (`/services`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/services` | No | Público | Listar servicios activos. Filtros: `categoriaId`, `especie`, `q`. |
| `GET` | `/services/{id}` | No | Público | Detalle de servicio. |
| `POST` | `/services` | Sí | `ADMIN` | Crear servicio. |
| `PUT` | `/services/{id}` | Sí | `ADMIN` | Actualizar servicio. |
| `DELETE` | `/services/{id}` | Sí | `ADMIN` | Eliminar/desactivar servicio. |

**Nota:** `VETERINARIO` solo puede ver servicios, **NO** crear/editar/eliminar.

### 3.6 Órdenes (`/orders`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `POST` | `/orders` | Sí | `CLIENTE` | Crear orden (checkout). Calcula totales y descuenta stock en transacción. |
| `GET` | `/orders/me` | Sí | `CLIENTE` | Listar órdenes del cliente autenticado. |
| `GET` | `/orders` | Sí | `ADMIN`, `VENDEDOR` | Listar todas las órdenes. `VENDEDOR` solo consulta. |
| `GET` | `/orders/{id}` | Sí | Propietario o `ADMIN`/`VENDEDOR` | Detalle de orden. Cliente solo puede ver sus propias órdenes. |
| `PATCH` | `/orders/{id}/status` | Sí | `ADMIN` | Cambiar estado de orden. `VENDEDOR` **NO** puede modificar. |

**Estados válidos:** `PAGADA`, `EN_PREPARACION`, `ENVIADA`, `ENTREGADA`, `CANCELADA`

**Reglas de negocio:**
- Validar stock suficiente antes de descontar
- Calcular precio con descuento por ítem
- Calcular total en servidor
- Generar `numero_orden` único
- Al cancelar, restaurar stock (si aplica)
- Operación transaccional

### 3.7 Solicitudes de Servicio (`/service-requests`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `POST` | `/service-requests` | Sí | `CLIENTE` | Crear solicitud de cita. |
| `GET` | `/service-requests/me` | Sí | `CLIENTE` | Listar solicitudes del cliente autenticado. |
| `GET` | `/service-requests` | Sí | `ADMIN`, `VETERINARIO` | Listar todas las solicitudes. |
| `GET` | `/service-requests/{id}` | Sí | Propietario o `ADMIN`/`VETERINARIO` | Detalle de solicitud. |
| `PATCH` | `/service-requests/{id}` | Sí | `ADMIN`, `VETERINARIO` | Actualizar fecha/hora, comentario o estado. |
| `PATCH` | `/service-requests/{id}/cancel` | Sí | Propietario (`CLIENTE`) | Cancelar solicitud solo si estado es `PENDIENTE`. |

**Estados válidos:** `PENDIENTE`, `CONFIRMADA`, `COMPLETADA`, `CANCELADA`  
**Nota:** `VENDEDOR` **NO** tiene acceso a este módulo.

### 3.8 Blog/Posts (`/posts`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/posts` | No | Público | Listar posts publicados. |
| `GET` | `/posts/{slug}` | No | Público | Obtener post por slug. |
| `POST` | `/posts` | Sí | `ADMIN` | Crear post. |
| `PUT` | `/posts/{id}` | Sí | `ADMIN` | Actualizar post. |
| `DELETE` | `/posts/{id}` | Sí | `ADMIN` | Eliminar/desactivar post. |

### 3.9 Dashboard/Métricas (`/dashboard`)
| Método | Endpoint | Auth | Roles | Descripción |
|---|---|---|---|---|
| `GET` | `/dashboard/metrics` | Sí | `ADMIN` | Métricas: conteo de órdenes, solicitudes, usuarios, productos. Productos con stock crítico. Últimas órdenes y solicitudes. |

## 4. DTOs Clave (Referencia)

### Categoría (Response)
```json
{
  "id": 1,
  "nombre": "Alimentos",
  "tipo": "PRODUCTO",
  "descripcion": "Alimentos balanceados para mascotas"
}
```

### Producto (Response)
```json
{
  "id": 1,
  "codigo": "PROD-001",
  "nombre": "Alimento Premium",
  "descripcion": "...",
  "presentacion": "15kg",
  "precio": 45000,
  "descuento": 10,
  "precioConDescuento": 40500,
  "stock": 50,
  "stockCritico": 5,
  "imagen": "/images/producto.jpg",
  "categoriaId": 1,
  "categoriaNombre": "Alimentos",
  "activo": true
}
```

### Producto (Create/Update Request)
```json
{
  "codigo": "PROD-001",
  "nombre": "Alimento Premium",
  "descripcion": "Descripción del producto",
  "presentacion": "15kg",
  "precio": 45000,
  "descuento": 10,
  "stock": 50,
  "stockCritico": 5,
  "imagen": "/images/producto.jpg",
  "categoriaId": 1,
  "activo": true
}
```

### Servicio (Response)
```json
{
  "id": 1,
  "codigo": "SERV-001",
  "nombre": "Consulta General",
  "descripcion": "Consulta veterinaria general",
  "precio": 25000,
  "duracionMinutos": 30,
  "imagen": "/images/consulta.jpg",
  "categoriaId": 2,
  "categoriaNombre": "Consultas",
  "activo": true,
  "especie": "GENERAL"
}
```

### Servicio (Create/Update Request)
```json
{
  "codigo": "SERV-001",
  "nombre": "Consulta General",
  "descripcion": "Consulta veterinaria general",
  "precio": 25000,
  "duracionMinutos": 30,
  "imagen": "/images/consulta.jpg",
  "categoriaId": 2,
  "activo": true,
  "especie": "GENERAL"
}
```

### Usuario (Response) - Nunca incluir password_hash
```json
{
  "id": 1,
  "run": "12345678-9",
  "email": "cliente@ejemplo.cl",
  "nombre": "Juan",
  "apellido": "Pérez",
  "fechaNacimiento": "1990-01-01",
  "telefono": "+56912345678",
  "direccion": "Calle 123",
  "region": "Metropolitana",
  "comuna": "Santiago",
  "rol": "CLIENTE",
  "activo": true,
  "fechaRegistro": "2026-10-24T10:30:00Z"
}
```

### Usuario (Create Request - Admin)
```json
{
  "run": "12345678-9",
  "email": "vendedor@ejemplo.cl",
  "password": "Password123!",
  "nombre": "Pedro",
  "apellido": "López",
  "fechaNacimiento": "1985-05-10",
  "telefono": "+56987654321",
  "direccion": "Av. Principal 456",
  "region": "Metropolitana",
  "comuna": "Providencia",
  "rol": "VENDEDOR",
  "activo": true
}
```

### Usuario (Update Request - Admin)
```json
{
  "nombre": "Pedro",
  "apellido": "López",
  "fechaNacimiento": "1985-05-10",
  "telefono": "+56987654321",
  "direccion": "Av. Principal 456",
  "region": "Metropolitana",
  "comuna": "Providencia",
  "rol": "VENDEDOR",
  "activo": true
}
```

### Orden (Request)
```json
{
  "items": [
    {
      "productoId": 1,
      "cantidad": 2
    }
  ],
  "direccionEnvio": "Calle 123, Depto 4",
  "tipoEntrega": "ENVIO",
  "instrucciones": "Dejar en portería",
  "contactoNombre": "Juan",
  "contactoApellido": "Pérez",
  "contactoEmail": "cliente@ejemplo.cl",
  "contactoTelefono": "+56912345678"
}
```

### Orden (Response)
```json
{
  "id": 1,
  "numeroOrden": "ORD-20261024-0001",
  "fechaOrden": "2026-10-24T14:30:00Z",
  "estado": "PAGADA",
  "subtotal": 81000,
  "total": 81000,
  "direccionEnvio": "Calle 123, Depto 4",
  "tipoEntrega": "ENVIO",
  "instrucciones": "Dejar en portería",
  "contactoNombre": "Juan",
  "contactoApellido": "Pérez",
  "contactoEmail": "cliente@ejemplo.cl",
  "contactoTelefono": "+56912345678",
  "usuarioId": 1,
  "items": [
    {
      "id": 1,
      "productoId": 1,
      "productoNombre": "Alimento Premium",
      "cantidad": 2,
      "precioUnitario": 45000,
      "descuento": 10,
      "precioUnitarioConDescuento": 40500,
      "subtotal": 81000
    }
  ]
}
```

### Cambiar Estado Orden (Request)
```json
{
  "estado": "EN_PREPARACION"
}
```

### Solicitud de Servicio (Request)
```json
{
  "servicioId": 1,
  "mascotaId": 2,
  "mascotaNombre": "Firulais",
  "especie": "Perro",
  "fechaSolicitada": "2026-10-25",
  "comentario": "Revisión general"
}
```

### Solicitud de Servicio (Response)
```json
{
  "id": 1,
  "servicioId": 1,
  "servicioNombre": "Consulta General",
  "usuarioId": 1,
  "usuarioNombre": "Juan Pérez",
  "mascotaId": 2,
  "mascotaNombre": "Firulais",
  "especie": "Perro",
  "fechaSolicitada": "2026-10-25",
  "fechaConfirmada": null,
  "comentario": "Revisión general",
  "comentarioVeterinario": null,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-10-24T10:00:00Z"
}
```

### Actualizar Solicitud de Servicio (Request - Admin/Veterinario)
```json
{
  "fechaConfirmada": "2026-10-25T15:30:00",
  "comentarioVeterinario": "Traer ayuno de 8 horas",
  "estado": "CONFIRMADA"
}
```

### Post (Response)
```json
{
  "id": 1,
  "titulo": "Cuidados de invierno para tu mascota",
  "slug": "cuidados-invierno-mascota",
  "contenido": "<p>Contenido HTML/Markdown...</p>",
  "imagen": "/images/blog/invierno.jpg",
  "autor": "Dr. Veterinario",
  "fechaPublicacion": "2026-10-20T08:00:00Z",
  "activo": true
}
```

### Post (Create/Update Request - Admin)
```json
{
  "titulo": "Cuidados de invierno para tu mascota",
  "slug": "cuidados-invierno-mascota",
  "contenido": "<p>Contenido HTML/Markdown...</p>",
  "imagen": "/images/blog/invierno.jpg",
  "autor": "Dr. Veterinario",
  "activo": true
}
```

### Dashboard Métricas (Response)
```json
{
  "totalOrdenes": 150,
  "totalSolicitudes": 45,
  "totalUsuarios": 80,
  "totalProductos": 32,
  "productosStockCritico": [
    {
      "id": 5,
      "codigo": "PROD-005",
      "nombre": "Vacuna Antirrábica",
      "stock": 3,
      "stockCritico": 5
    }
  ],
  "ultimasOrdenes": [
    {
      "id": 150,
      "numeroOrden": "ORD-20261024-0150",
      "fechaOrden": "2026-10-24T16:00:00Z",
      "total": 45000,
      "estado": "PAGADA",
      "clienteNombre": "Juan Pérez"
    }
  ],
  "ultimasSolicitudes": [
    {
      "id": 45,
      "servicioNombre": "Consulta General",
      "fechaSolicitada": "2026-10-25",
      "estado": "PENDIENTE",
      "clienteNombre": "María González"
    }
  ]
}
```

## 5. Matriz Unificada de Permisos (Endpoint x Rol)

| Endpoint | Público | CLIENTE | VENDEDOR | VETERINARIO | ADMIN |
|---|---|---|---|---|---|
| `POST /auth/register`, `POST /auth/login` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `GET /auth/me` | - | ✓ | ✓ | ✓ | ✓ |
| `GET /users/**`, `POST /users`, `PUT /users/{id}`, `DELETE /users/{id}` | - | - | - | - | ✓ |
| `GET /categories?type=*` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `GET /products`, `GET /products/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}` | - | - | - | - | ✓ |
| `GET /services`, `GET /services/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /services`, `PUT /services/{id}`, `DELETE /services/{id}` | - | - | - | - | ✓ |
| `POST /orders` | - | ✓ | - | - | ✓ |
| `GET /orders/me` | - | ✓ | - | - | ✓ |
| `GET /orders`, `GET /orders/{id}` | - | Propio* | ✓ (solo lectura) | - | ✓ |
| `PATCH /orders/{id}/status` | - | - | - | - | ✓ |
| `POST /service-requests` | - | ✓ | - | - | ✓ |
| `GET /service-requests/me` | - | ✓ | - | - | ✓ |
| `GET /service-requests`, `GET /service-requests/{id}` | - | Propio* | - | ✓ | ✓ |
| `PATCH /service-requests/{id}` | - | - | - | ✓ | ✓ |
| `PATCH /service-requests/{id}/cancel` | - | Propio (PENDIENTE) | - | - | ✓ |
| `GET /posts`, `GET /posts/{slug}` | ✓ | ✓ | ✓ | ✓ | ✓ |
| `POST /posts`, `PUT /posts/{id}`, `DELETE /posts/{id}` | - | - | - | - | ✓ |
| `GET /dashboard/metrics` | - | - | - | - | ✓ |
| `GET /swagger-ui/**`, `/v3/api-docs/**` | ✓ | ✓ | ✓ | ✓ | ✓ |

\* Solo puede acceder a sus propios recursos.

## 6. Seguridad

- **JWT:** Token firmado, expiración configurable (`JWT_EXPIRATION`, default 7200s)
- **Endpoints públicos:** `/auth/**`, `/products/**` (GET), `/services/**` (GET), `/categories/**` (GET), `/posts/**` (GET), Swagger UI y OpenAPI docs
- **Endpoints protegidos:** Requieren header `Authorization: Bearer {token}`
- **Autorización por roles:** Usar `@PreAuthorize` o configuración en `SecurityFilterChain`
- **CORS:** Configurado para `http://localhost:5173`, `http://localhost:4173`, `https://ncatalanv.github.io`

## 7. Checklist de Validación

- [x] Todos los endpoints mapeados según esta especificación
- [x] Matriz de roles aplicada en backend (seguridad) y frontend (rutas/sidebar)
- [x] DTOs con validación (`@Valid`, Bean Validation)
- [x] Errores en formato Problem Details
- [x] Transacciones en operaciones críticas (órdenes)
- [x] Documentación actualizada en Swagger
- [x] Permisos probados por cada rol
- [x] Contrato guardado en `API_CONTRACT.md`
- [x] Revisado en conjunto BE+FE
