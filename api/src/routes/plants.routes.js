import express from "express";
import pool from "../db/connection.js";
import { v4 as uuidv4 } from "uuid";

const route = express.Router();

route
  .get("/plantas", async (req, res) => {
    try {
      // Consulta para obtener todos los usuarios, sus plantas y los servicios asociados
      const result = await pool.query(
        `SELECT u.id AS user_id, u.name AS user_name, u.email AS user_email, 
                p.id AS planta_id, p.name AS planta_name, 
                h.id AS historial_id, h.servicio_id, h.fecha, 
                s.name AS servicio_name, s.description AS servicio_description
         FROM usuarios u
         LEFT JOIN plantas p ON u.id = p.usuario_id
         LEFT JOIN historiales h ON p.id = h.planta_id
         LEFT JOIN servicios s ON h.servicio_id = s.id`
      );

      // Verificar si hay usuarios
      if (result.rows.length === 0) {
        return res.status(404).json({ message: "No se encontraron usuarios" });
      }

      // Agrupar los historiales y las plantas por usuario
      const users = [];
      result.rows.forEach((row) => {
        let user = users.find((u) => u.user_id === row.user_id);

        if (!user) {
          user = {
            user_id: row.user_id,
            user_name: row.user_name,
            user_email: row.user_email,
            plantas: [],
          };
          users.push(user);
        }

        let plant = user.plantas.find((p) => p.planta_id === row.planta_id);

        if (!plant) {
          plant = {
            planta_id: row.planta_id,
            planta_name: row.planta_name,
            historiales: [],
          };
          user.plantas.push(plant);
        }

        if (row.historial_id) {
          plant.historiales.push({
            historial_id: row.historial_id,
            servicio_id: row.servicio_id,
            fecha: row.fecha,
            servicio_name: row.servicio_name,
            servicio_description: row.servicio_description,
          });
        }
      });

      // Responder con los usuarios, sus plantas y servicios
      res.status(200).json(users);
    } catch (err) {
      console.error(
        "Error al obtener los usuarios, sus plantas y servicios:",
        err
      );
      res
        .status(500)
        .json({
          error: "Error al obtener los usuarios, sus plantas y servicios",
        });
    }
  })
  .get("/plants/:id", async (req, res) => {
    const { id } = req.params;

    try {
      // Consulta para obtener la planta con su historial
      const result = await pool.query(
        `SELECT p.id AS planta_id, p.name AS planta_name, 
            h.id AS historial_id, h.servicio_id, h.fecha, 
            s.name AS servicio_name, s.description AS servicio_description
       FROM plantas p
       LEFT JOIN historiales h ON p.id = h.planta_id
       LEFT JOIN servicios s ON h.servicio_id = s.id
       WHERE p.id = $1`,
        [id]
      );

      // Verificar si la planta fue encontrada
      if (result.rows.length === 0) {
        return res.status(404).json({ message: "Planta no encontrada." });
      }

      // Agrupar los historiales por planta y excluir historiales sin información
      const plant = result.rows[0];
      const historiales = result.rows
        .filter((row) => row.historial_id !== null) // Excluir registros sin historial
        .map((row) => ({
          historial_id: row.historial_id,
          servicio_id: row.servicio_id,
          fecha: row.fecha,
          servicio_name: row.servicio_name,
          servicio_description: row.servicio_description,
        }));

      // Construir la respuesta
      res.status(200).json({
        planta: {
          id: plant.planta_id,
          name: plant.planta_name,
          historiales: historiales.length > 0 ? historiales : [], // Devolver un array vacío si no hay historiales
        },
      });
    } catch (err) {
      console.error("Error al obtener las plantas y sus historiales:", err);
      res
        .status(500)
        .json({ error: "Error al obtener las plantas y sus historiales" });
    }
  })

  .get("/plants", async (req, res) => {
    try {
      // Realizar la consulta para obtener todas las plantas
      const result = await pool.query("SELECT * FROM plantas");

      // Verificar si se encontraron plantas
      if (result.rows.length === 0) {
        return res.status(404).json({ message: "No se encontraron plantas" });
      }

      // Responder con las plantas encontradas
      res.status(200).json(result.rows);
    } catch (err) {
      // Manejo de errores
      console.error("Error al obtener las plantas:", err);
      res.status(500).json({ error: "Error al obtener las plantas" });
    }
  })
  .get("/plants/user/:userId", async (req, res) => {
    const { userId } = req.params; // Obtener el ID del usuario desde la URL

    try {
      // Realizar la consulta para obtener las plantas del usuario
      const result = await pool.query(
        "SELECT * FROM plantas WHERE usuario_id = $1",
        [userId]
      );

      // Verificar si se encontraron plantas para el usuario
      if (result.rows.length === 0) {
        return res
          .status(404)
          .json({ message: "No se encontraron plantas para este usuario" });
      }

      // Responder con las plantas encontradas
      res.status(200).json(result.rows);
    } catch (err) {
      // Manejo de errores
      console.error("Error al obtener las plantas del usuario:", err);
      res
        .status(500)
        .json({ error: "Error al obtener las plantas del usuario" });
    }
  })
  .post("/plants/create", async (req, res) => {
    const { name, usuario_id } = req.body;

    // Verificar si los datos requeridos están presentes
    if (!name || !usuario_id) {
      return res
        .status(400)
        .json({ message: "Faltan datos requeridos: name y usuario_id" });
    }

    try {
      // Generar una ID única
      const id = uuidv4();

      // Realizar la inserción de la nueva planta
      const result = await pool.query(
        "INSERT INTO plantas (id, name, usuario_id) VALUES ($1, $2, $3) RETURNING *",
        [id, name, usuario_id]
      );

      // Responder con la planta creada
      res.status(201).json({
        message: "Planta creada exitosamente",
        planta: result.rows[0], // Devolver la planta recién creada
      });
    } catch (err) {
      // Manejo de errores
      console.error("Error al crear la planta:", err);
      res.status(500).json({ error: "Error al crear la planta" });
    }
  })
  .delete("/plants/delete/:id", async (req, res) => {
    const { id } = req.params;

    // Verificar si el ID está presente
    if (!id) {
      return res
        .status(400)
        .json({ message: "Falta el ID de la planta para eliminar." });
    }

    try {
      // Realizar la consulta para verificar si la planta existe
      const result = await pool.query("SELECT * FROM plantas WHERE id = $1", [
        id,
      ]);

      // Si la planta no existe
      if (result.rows.length === 0) {
        return res.status(404).json({ message: "Planta no encontrada." });
      }

      // Eliminar la planta
      await pool.query("DELETE FROM plantas WHERE id = $1", [id]);

      // Responder con éxito
      res.status(200).json({ message: "Planta eliminada exitosamente." });
    } catch (err) {
      // Manejo de errores
      console.error("Error al eliminar la planta:", err);
      res.status(500).json({ error: "Error al eliminar la planta" });
    }
  })
  .post("/plants/add-services", async (req, res) => {
    const { plantaId, servicioIds } = req.body;

    try {
      // Verificar que plantaId y servicioIds sean válidos
      if (
        !plantaId ||
        !Array.isArray(servicioIds) ||
        servicioIds.length === 0
      ) {
        return res.status(400).json({
          message: "PlantaId y una lista de servicioIds son requeridos.",
        });
      }

      // Verificar si la planta existe
      const plantaResult = await pool.query(
        "SELECT id FROM plantas WHERE id = $1",
        [plantaId]
      );
      if (plantaResult.rowCount === 0) {
        return res.status(404).json({ message: "Planta no encontrada." });
      }

      // Verificar si los servicios existen
      const validServicesResult = await pool.query(
        "SELECT id FROM servicios WHERE id = ANY($1::int[])",
        [servicioIds]
      );
      const validServiceIds = validServicesResult.rows.map((row) => row.id);

      if (validServiceIds.length !== servicioIds.length) {
        return res.status(400).json({
          message: "Algunos servicios no son válidos.",
          validServiceIds,
        });
      }

      // Crear los registros en el historial
      const fecha = new Date();
      const historialPromises = servicioIds.map((servicioId) => {
        return pool.query(
          `INSERT INTO historiales (id, planta_id, servicio_id, fecha) 
           VALUES ($1, $2, $3, $4)`,
          [uuidv4(), plantaId, servicioId, fecha]
        );
      });

      await Promise.all(historialPromises);

      // Responder con éxito
      res.status(201).json({
        message: "Servicios añadidos a la planta exitosamente.",
        plantaId,
        servicioIds,
      });
    } catch (error) {
      console.error("Error al añadir servicios a la planta:", error);
      res
        .status(500)
        .json({ message: "Error al añadir servicios a la planta." });
    }
  });
export default route;
