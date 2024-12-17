import express from "express";
import pool from "../db/connection.js";
import bcrypt from "bcrypt";
import { v4 as uuidv4 } from "uuid";

const route = express.Router();

route.get("/services", async (req, res) => {
  try {
    // Consulta para obtener todos los servicios
    const result = await pool.query(
      "SELECT id, name, description, created_at, updated_at FROM servicios"
    );

    // Verificar si se encontraron servicios
    if (result.rows.length === 0) {
      return res.status(404).json({ message: "No se encontraron servicios" });
    }

    // Responder con la lista de servicios como un arreglo JSON
    res.status(200).json(result.rows);
  } catch (err) {
    // Manejo de errores
    console.error("Error al obtener los servicios:", err);
    res.status(500).json({ error: "Error al obtener los servicios" });
  }
});

export default route;
