import express from "express";
import pool from "../db/connection.js";
import bcrypt from "bcrypt";
import { v4 as uuidv4 } from "uuid";
import jwt from "jsonwebtoken";

const route = express.Router();

route
  .get("/users", async (req, res) => {
    try {
      // Consulta para obtener todos los usuarios
      const result = await pool.query(
        "SELECT id, name, email, created_at, updated_at FROM usuarios"
      );

      // Verificar si se encontraron plantas
      if (result.rows.length === 0) {
        return res.status(404).json({ message: "No se encontraron usuarios" });
      }

      // Responder con la lista de usuarios
      res.status(200).json(
        result.rows // Lista de usuarios
      );
    } catch (err) {
      // Manejo de errores
      console.error("Error al obtener los usuarios:", err);
      res.status(500).json({ error: "Error al obtener los usuarios" });
    }
  })
  .post("/users/create", async (req, res) => {
    const { name, email, password } = req.body;

    // Verificar si los datos requeridos están presentes
    if (!name || !email || !password) {
      return res
        .status(400)
        .json({ message: "Faltan datos requeridos: name, email y password" });
    }

    try {
      // Verificar si ya existe un usuario con el mismo correo
      const existingUser = await pool.query(
        "SELECT * FROM usuarios WHERE email = $1",
        [email]
      );
      if (existingUser.rows.length > 0) {
        return res
          .status(400)
          .json({ message: "Ya existe un usuario con ese email" });
      }

      // Encriptar la contraseña
      const hashedPassword = await bcrypt.hash(password, 10);

      // Generar un id único con uuid
      const id = uuidv4();

      // Insertar el nuevo usuario en la base de datos
      const result = await pool.query(
        "INSERT INTO usuarios (id, name, email, password) VALUES ($1, $2, $3, $4) RETURNING *",
        [id, name, email, hashedPassword]
      );

      // Responder con el usuario creado
      res.status(201).json({
        message: "Usuario creado exitosamente",
        user: result.rows[0], // Devolver el usuario recién creado
      });
    } catch (err) {
      // Manejo de errores
      console.error("Error al crear el usuario:", err);
      res.status(500).json({ error: "Error al crear el usuario" });
    }
  })
  .post("/users/login", async (req, res) => {
    const JWT_SECRET = "secreto";
    const { email, password } = req.body;

    // Verificar si los datos requeridos están presentes
    if (!email || !password) {
      return res
        .status(400)
        .json({ message: "Faltan datos requeridos: email y password" });
    }

    try {
      // Buscar al usuario por su email
      const userResult = await pool.query(
        "SELECT * FROM usuarios WHERE email = $1",
        [email]
      );

      if (userResult.rows.length === 0) {
        return res.status(400).json({ message: "Usuario no encontrado" });
      }

      const user = userResult.rows[0];

      // Verificar la contraseña
      const isPasswordValid = await bcrypt.compare(password, user.password);

      if (!isPasswordValid) {
        return res.status(401).json({ message: "Contraseña incorrecta" });
      }

      // Crear el token JWT
      const token = jwt.sign(
        {
          id: user.id,
          email: user.email,
          name: user.name,
        },
        JWT_SECRET,
        { expiresIn: "1h" } // El token expira en 1 hora
      );

      // Responder con el token
      res.status(200).json({
        message: "Inicio de sesión exitoso",
        token,
      });
    } catch (err) {
      console.error("Error al iniciar sesión:", err);
      res.status(500).json({ error: "Error al iniciar sesión" });
    }
  })
  .get("/users/get/:id", async (req, res) => {
    const userId = req.params.id; // Obtener el ID del usuario desde la URL

    try {
      // Consulta para obtener los datos del usuario por ID
      const result = await pool.query(
        "SELECT id, name, email FROM usuarios WHERE id = $1",
        [userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ message: "Usuario no encontrado" });
      }

      res.status(200).json(result.rows[0]); // Devuelve los datos del usuario
    } catch (err) {
      console.error("Error al obtener el usuario:", err);
      res.status(500).json({ error: "Error al obtener el usuario" });
    }
  });

export default route;
