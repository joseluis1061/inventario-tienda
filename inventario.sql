-- Crear base de datos
CREATE DATABASE IF NOT EXISTS inventario_tienda;
USE inventario_tienda;

-- Tabla de Roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Usuarios
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rol_id BIGINT NOT NULL,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Tabla de Categorías
CREATE TABLE categorias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Productos
CREATE TABLE productos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    categoria_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

-- Tabla de Movimientos
CREATE TABLE movimientos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_movimiento ENUM('ENTRADA', 'SALIDA') NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    motivo VARCHAR(255),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_productos_stock_bajo ON productos(stock_actual, stock_minimo);
CREATE INDEX idx_movimientos_producto ON movimientos(producto_id);
CREATE INDEX idx_movimientos_usuario ON movimientos(usuario_id);
CREATE INDEX idx_movimientos_fecha ON movimientos(fecha);
CREATE INDEX idx_usuarios_username ON usuarios(username);

-- Insertar roles básicos
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema con acceso completo'),
('GERENTE', 'Gerente con permisos de gestión de inventario y usuarios'),
('EMPLEADO', 'Empleado con permisos básicos de consulta y registro');

-- Insertar categorías de ejemplo
INSERT INTO categorias (nombre, descripcion) VALUES
('Electrónicos', 'Dispositivos electrónicos y tecnología'),
('Ropa', 'Prendas de vestir y accesorios'),
('Hogar', 'Artículos para el hogar'),
('Deportes', 'Equipos y artículos deportivos'),
('Libros', 'Libros y material de lectura');

-- Insertar usuario administrador por defecto (password: admin123)
INSERT INTO usuarios (username, password, nombre_completo, email, rol_id) VALUES
('admin', '$2a$10$N.kmcuVb78KtCrvkJfbn4.9z7POt1c/Ls/zqJ6f/l3hGEJd9nTu5G', 'Administrador Sistema', 'admin@tienda.com', 1);

-- Vista para consultas frecuentes
CREATE VIEW vista_productos_stock AS
SELECT
    p.id,
    p.nombre,
    p.precio,
    p.stock_actual,
    p.stock_minimo,
    c.nombre as categoria,
    CASE
        WHEN p.stock_actual <= p.stock_minimo THEN 'CRÍTICO'
        WHEN p.stock_actual <= (p.stock_minimo * 1.5) THEN 'BAJO'
        ELSE 'NORMAL'
    END as estado_stock
FROM productos p
JOIN categorias c ON p.categoria_id = c.id;

-- Vista para historial de movimientos
CREATE VIEW vista_historial_movimientos AS
SELECT
    m.id,
    p.nombre as producto,
    u.nombre_completo as usuario,
    m.tipo_movimiento,
    m.cantidad,
    m.motivo,
    m.fecha,
    c.nombre as categoria
FROM movimientos m
JOIN productos p ON m.producto_id = p.id
JOIN usuarios u ON m.usuario_id = u.id
JOIN categorias c ON p.categoria_id = c.id
ORDER BY m.fecha DESC;