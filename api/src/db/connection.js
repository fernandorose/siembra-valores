import pg from "pg";

const pool = new pg.Pool({
  user: "admin", // Usuario de la base de datos
  host: "localhost", // Host del contenedor de PostgreSQL (si está en Docker, usa el nombre del contenedor)
  database: "siembravalores", // Nombre de la base de datos
  password: "Siembra123", // Contraseña de la base de datos
  port: 5432, // Puerto de PostgreSQL
});

export default pool;
