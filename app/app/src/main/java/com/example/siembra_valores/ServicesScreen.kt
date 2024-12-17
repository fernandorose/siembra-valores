package com.example.siembra_valores

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ServicesScreen(navController: NavController, plantId: String) {
  var services by remember { mutableStateOf<List<Service>>(emptyList()) }
  var selectedServices by remember { mutableStateOf<List<String>>(emptyList()) }
  var errorMessage by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()
  val customFont = FontFamily(
    Font(R.font.bricolage400, FontWeight.Normal),
    Font(R.font.bricolage800, FontWeight.Bold),
    Font(R.font.bricolage700, FontWeight.Medium),
  )

  // Obtener los servicios cuando la pantalla se carga
  LaunchedEffect(Unit) {
    coroutineScope.launch {
      try {
        // Llamada a la API para obtener los servicios
        val response = RetrofitInstance.api.getServices()
        services = response
      } catch (e: Exception) {
        errorMessage = e.message ?: "Error al obtener los servicios"
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(30.dp)
      .systemBarsPadding()
  ) {
    if (errorMessage.isNotEmpty()) {
      Text(text = "Error: $errorMessage", color = Color.Red)
    } else {
      Text(
        text = "Atrás",
        fontWeight = FontWeight.Bold,
        fontFamily = customFont,
        modifier = Modifier
          .clickable { navController.popBackStack() }
          .padding(bottom = 10.dp)
      )
      Text(
        text = "Selecciona los servicios para la planta",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 16.dp)
      )
      services.forEach { service ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
              selectedServices = if (selectedServices.contains(service.id)) {
                selectedServices.filterNot { it == service.id }
              } else {
                selectedServices + service.id
              }
            },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = selectedServices.contains(service.id),
            onCheckedChange = { isChecked ->
              selectedServices = if (isChecked) {
                selectedServices + service.id
              } else {
                selectedServices.filterNot { it == service.id }
              }
            }
          )
          Text(
            text = service.name,
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
          )
        }
      }

      Button(
        onClick = {
          // Realizar la solicitud para agregar los servicios a la planta
          coroutineScope.launch {
            try {
              val addServicesRequest = AddServicesRequest(
                plantaId = plantId,
                servicioIds = selectedServices
              )
              val response = RetrofitInstance.api.addServicesToPlant(addServicesRequest)
              if (response.isSuccessful) {
                navController.popBackStack()  // Regresar después de aplicar los servicios
              } else {
                errorMessage = "Error al aplicar los servicios"
              }
            } catch (e: Exception) {
              errorMessage = e.message ?: "Error al aplicar los servicios"
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Black,
          contentColor = Color.White
        ),
      ) {
        Text(text = "Aplicar Servicios")
      }
    }
  }
}
