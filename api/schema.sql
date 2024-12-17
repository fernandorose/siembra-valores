-- Crear tabla de Usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de Plantas
CREATE TABLE plantas (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    usuario_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Crear tabla de Servicios
CREATE TABLE servicios (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de Historial
CREATE TABLE historiales (
    id UUID PRIMARY KEY,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    planta_id UUID NOT NULL,
    servicio_id INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (planta_id) REFERENCES plantas(id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE
);

INSERT INTO servicios (name, description) 
VALUES
    ('Riego', 'Servicio de riego para mantener la planta hidratada'),
    ('Fumigar', 'Servicio de fumigación para proteger la planta de plagas'),
    ('Poda', 'Servicio de poda para cortar las partes no deseadas o muertas de la planta'),
    ('Fertilización', 'Servicio de fertilización para enriquecer el suelo con nutrientes'),
    ('Medir', 'Servicio para medir el crecimiento y estado de la planta');