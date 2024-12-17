package com.example.siembra_valores

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


// Retrofit Models
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val message: String, val token: String)

// Retrofit ApiService
interface ApiServiceLogin {
  @POST("/api/users/login")
  suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}

// RetrofitClient
object RetrofitClient {
  private const val BASE_URL = "http://192.168.100.9:3000" // Cambia esto por la URL de tu servidor

  val apiService: ApiServiceLogin by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiServiceLogin::class.java)
  }
}

@Composable
fun LoginScreen(navController: NavController, context: Context) {
  val customFont = FontFamily(
    Font(R.font.bricolage400, FontWeight.Normal),
    Font(R.font.bricolage800, FontWeight.Bold)
  )
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var loading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  val coroutineScope = rememberCoroutineScope() // Crear un CoroutineScope para lanzar la llamada

  Column(
    modifier = Modifier
      .fillMaxSize()
      .height(200.dp)
      .padding(start = 20.dp, end = 20.dp)
      .systemBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(0.dp), modifier = Modifier.padding(top = 30.dp)
    ) {
      Text(
        text = "Siembra Valores",
        fontSize = 50.sp,
        fontFamily = customFont,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "App para el control de tus plantas",
        fontFamily = customFont,
      )
    }
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(text = "Correo electronico", fontFamily = customFont, fontSize = 20.sp)
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = { email = it },
      )
      Text(text = "Contraseña", fontFamily = customFont, fontSize = 20.sp)
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = { password = it },
      )
      Button(
        onClick = {
          if (email.isNotEmpty() && password.isNotEmpty()) {
            loading = true
            errorMessage = "" // Reiniciar el mensaje de error
            coroutineScope.launch {
              try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                val token = response.token
                saveToken(context, token) // Guarda el token
                loading = false
                navController.navigate("users") // Navega a la siguiente pantalla
              } catch (e: Exception) {
                // Manejar error (ej. credenciales incorrectas o problemas de red)
                loading = false
                errorMessage = "Credenciales incorrectas. Por favor, intenta de nuevo."
              }
            }
          } else {
            errorMessage = "Por favor, ingrese todos los campos."
          }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Black,
          contentColor = Color.White
        )
      ) {
        if (loading) {
          Text(text = "Cargando...")
        } else {
          Text(
            text = "Ingresar",
            fontFamily = customFont,
            fontSize = 20.sp,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

    }

    if (errorMessage.isNotEmpty()) {
      Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
    }

    Row(
      modifier = Modifier.padding(bottom = 50.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Text(text = "No tienes cuenta?", fontFamily = customFont)
      Text(
        text = "Crea una aquí",
        fontFamily = customFont,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable {
          navController.navigate("register")
        }
      )
    }
  }
}