CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    run VARCHAR(15) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    fecha_nacimiento DATE NULL,
    telefono VARCHAR(20) NULL,
    direccion VARCHAR(255) NULL,
    region VARCHAR(80) NULL,
    comuna VARCHAR(80) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_run UNIQUE (run),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id) ON DELETE RESTRICT,
    INDEX idx_usuarios_rol (rol_id),
    INDEX idx_usuarios_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mascotas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raza VARCHAR(80) NULL,
    fecha_nacimiento DATE NULL,
    sexo VARCHAR(20) NULL,
    peso DECIMAL(5,2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_mascotas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_mascotas_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nombre_tipo UNIQUE (nombre, tipo),
    CONSTRAINT chk_categorias_tipo CHECK (tipo IN ('PRODUCTO', 'SERVICIO')),
    INDEX idx_categorias_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE productos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT NULL,
    presentacion VARCHAR(120) NULL,
    precio DECIMAL(12,2) NOT NULL,
    descuento INT NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    stock_critico INT NOT NULL DEFAULT 5,
    imagen VARCHAR(255) NULL,
    categoria_id BIGINT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_productos_codigo UNIQUE (codigo),
    CONSTRAINT chk_productos_precio CHECK (precio >= 0),
    CONSTRAINT chk_productos_descuento CHECK (descuento BETWEEN 0 AND 100),
    CONSTRAINT chk_productos_stock CHECK (stock >= 0),
    CONSTRAINT chk_productos_stock_critico CHECK (stock_critico >= 0),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE SET NULL,
    INDEX idx_productos_categoria (categoria_id),
    INDEX idx_productos_activo (activo),
    INDEX idx_productos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE servicios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT NULL,
    especie VARCHAR(80) NULL,
    duracion VARCHAR(40) NULL,
    precio DECIMAL(12,2) NOT NULL,
    imagen VARCHAR(255) NULL,
    nota VARCHAR(255) NULL,
    categoria_id BIGINT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_servicios_codigo UNIQUE (codigo),
    CONSTRAINT chk_servicios_precio CHECK (precio >= 0),
    CONSTRAINT fk_servicios_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE SET NULL,
    INDEX idx_servicios_categoria (categoria_id),
    INDEX idx_servicios_activo (activo),
    INDEX idx_servicios_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ordenes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero_orden VARCHAR(30) NOT NULL,
    usuario_id BIGINT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    direccion_envio VARCHAR(255) NOT NULL,
    tipo_entrega VARCHAR(20) NOT NULL,
    instrucciones VARCHAR(500) NULL,
    contacto_nombre VARCHAR(80) NULL,
    contacto_apellido VARCHAR(80) NULL,
    contacto_email VARCHAR(120) NULL,
    contacto_telefono VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_ordenes_numero UNIQUE (numero_orden),
    CONSTRAINT chk_ordenes_total CHECK (total >= 0),
    CONSTRAINT chk_ordenes_estado CHECK (estado IN ('PAGADA', 'EN_PREPARACION', 'ENVIADA', 'ENTREGADA', 'CANCELADA')),
    CONSTRAINT chk_ordenes_tipo_entrega CHECK (tipo_entrega IN ('ENVIO', 'RETIRO')),
    CONSTRAINT fk_ordenes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_ordenes_usuario (usuario_id),
    INDEX idx_ordenes_estado (estado),
    INDEX idx_ordenes_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE orden_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orden_id BIGINT NOT NULL,
    producto_id BIGINT NULL,
    producto_nombre VARCHAR(120) NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    descuento INT NOT NULL DEFAULT 0,
    cantidad INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orden_detalle_orden_producto UNIQUE (orden_id, producto_id),
    CONSTRAINT chk_orden_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_orden_detalle_descuento CHECK (descuento BETWEEN 0 AND 100),
    CONSTRAINT chk_orden_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_orden_detalle_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_orden_detalle_orden FOREIGN KEY (orden_id) REFERENCES ordenes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_orden_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos (id) ON DELETE SET NULL,
    INDEX idx_orden_detalle_orden (orden_id),
    INDEX idx_orden_detalle_producto (producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE solicitudes_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    mascota_id BIGINT NULL,
    mascota_nombre VARCHAR(80) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    fecha_solicitada DATE NULL,
    fecha_cita DATE NULL,
    hora_cita TIME NULL,
    comentario VARCHAR(300) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_solicitudes_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA')),
    CONSTRAINT fk_solicitudes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas (id) ON DELETE SET NULL,
    INDEX idx_solicitudes_usuario (usuario_id),
    INDEX idx_solicitudes_servicio (servicio_id),
    INDEX idx_solicitudes_mascota (mascota_id),
    INDEX idx_solicitudes_estado (estado),
    INDEX idx_solicitudes_fecha_cita (fecha_cita)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(150) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    categoria VARCHAR(80) NULL,
    extracto VARCHAR(500) NULL,
    imagen VARCHAR(255) NULL,
    alt VARCHAR(255) NULL,
    fecha DATE NOT NULL,
    contenido JSON NOT NULL,
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_posts_slug UNIQUE (slug),
    INDEX idx_posts_fecha (fecha),
    INDEX idx_posts_publicado (publicado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO roles (nombre) VALUES
    ('ADMIN'),
    ('VENDEDOR'),
    ('VETERINARIO'),
    ('CLIENTE');
