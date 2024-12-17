package com.example.siembra_valores

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

data class PlantWithHistory(
  val planta: Plant,
)

data class History(
  val historial_id: String,
  val servicio_id: String,
  val fecha: String,
  val servicio_name: String,
  val servicio_description: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlantScreen(navController: NavController, plantId: String) {
  var plant by remember { mutableStateOf<Plant?>(null) }
  var errorMessage by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()
  val customFont = FontFamily(
    Font(R.font.bricolage400, FontWeight.Normal),
    Font(R.font.bricolage800, FontWeight.Bold),
    Font(R.font.bricolage700, FontWeight.Medium),
  )
  val monoFont = FontFamily(
    Font(R.font.jetbrainsmono400, FontWeight.Normal)
  )

  LaunchedEffect(plantId) {
    coroutineScope.launch {
      try {
        val response = RetrofitInstance.api.getPlantById(plantId)
        plant = response.planta
      } catch (e: Exception) {
        errorMessage = e.message ?: "Error al obtener los detalles de la planta"
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .systemBarsPadding()
  ) {
    if (errorMessage.isNotEmpty()) {
      Text(text = "Error: $errorMessage", color = Color.Red)
    } else {
      plant?.let {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Atrás",
            fontWeight = FontWeight.Bold,
            fontFamily = customFont,
            modifier = Modifier
              .clickable { navController.popBackStack() }
              .padding(bottom = 10.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = it.name,
              fontWeight = FontWeight.Bold,
              fontSize = 40.sp,
              fontFamily = customFont,
            )
            Button(
              onClick = {
                navController.navigate("plant_service_screen/${it.id}")
              },
              shape = RoundedCornerShape(10),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
              ),
            ) {
              Text(text = "Hacer servicio")
            }

          }
          Text(
            text = "ID de Planta: ${it.id}",
            fontSize = 10.sp,
            fontFamily = monoFont,
          )
          Column(modifier = Modifier.fillMaxWidth()) {
            // Botón para borrar la planta
            Button(
              onClick = {
                coroutineScope.launch {
                  try {
                    val response = RetrofitInstance.api.deletePlant(plantId)
                    if (response.isSuccessful) {
                      navController.popBackStack()  // Regresar a la pantalla anterior después de eliminar
                    } else {
                      errorMessage = "Error al eliminar la planta."
                    }
                  } catch (e: Exception) {
                    errorMessage = e.message ?: "Error al eliminar la planta"
                  }
                }
              },
              modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
              shape = RoundedCornerShape(10),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
              ),
            ) {
              Text(text = "Eliminar Planta")
            }
          }
          Text(
            text = "Historial de Servicios:",
            fontWeight = FontWeight.Bold,
            fontFamily = customFont,
          )

          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            item {
              // Encabezado o texto si no hay historiales
              if (it.historiales.isEmpty()) {
                Text(
                  text = "Sin servicios",
                  color = Color.Gray,
                  fontFamily = customFont,
                  modifier = Modifier.padding(8.dp)
                )
              }
            }

            // Mostrar la lista de historiales si existen
            items(it.historiales) { history ->
              val formattedDate = formatDate(history.fecha)
              Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                  text = history.servicio_name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  fontFamily = customFont,
                )
                Text(
                  text = "Descripción: ${history.servicio_description}",
                  fontFamily = customFont,
                )
                Text(
                  text = "Fecha: $formattedDate",
                  fontFamily = customFont,
                )
              }
            }
          }
        }
      }
    }
  }
}


