package com.example.siembra_valores

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(isoDate: String): String {
  // Analiza la fecha en formato ISO 8601 y convierte a Instant
  val instant = Instant.parse(isoDate)

  // Define el formato de salida deseado
  val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    .withZone(ZoneId.systemDefault())

  // Formatea la fecha
  return outputFormatter.format(instant)
}