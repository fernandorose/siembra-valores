import express from "express";
import cors from "cors";

import plantsRoutes from "./routes/plants.routes.js";
import usersRoutes from "./routes/users.routes.js";
import servicesRoutes from "./routes/services.routes.js";

import pool from "./db/connection.js";

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(cors());

app.use("/api", plantsRoutes);
app.use("/api", usersRoutes);
app.use("/api", servicesRoutes);

const checkDBConnection = async () => {
  try {
    await pool.query("SELECT 1");
    console.log("DB connection successfully");
  } catch (error) {
    console.log(error);
    process.exit(1);
  }
};

const main = async () => {
  await checkDBConnection();
  app.listen(PORT, () => {
    console.log(`api running on port: ${PORT}`);
  });

  try {
    console.log("Servicios por defecto insertados correctamente.");
  } catch (error) {
    console.error("Error al insertar los servicios por defecto:", error);
  }
};

main();
