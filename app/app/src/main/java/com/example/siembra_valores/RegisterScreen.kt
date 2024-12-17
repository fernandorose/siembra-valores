package com.example.siembra_valores

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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

@Composable
fun RegisterScreen(navController: NavController) {
  val customFont = FontFamily(
    Font(R.font.bricolage400, FontWeight.Normal),
    Font(R.font.bricolage800, FontWeight.Bold)
  )

  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf("") }
  var successMessage by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(start = 20.dp, end = 20.dp)
      .systemBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(0.dp),
      modifier = Modifier.padding(top = 30.dp)
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
      Text(text = "Nombre", fontFamily = customFont, fontSize = 20.sp)
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = name,
        onValueChange = { name = it },
      )
      Text(text = "Correo electrónico", fontFamily = customFont, fontSize = 20.sp)
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
          registerUser(name, email, password,
            onSuccess = { successMessage = "Registro exitoso" },
            onError = { errorMessage = it }
          )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Black,
          contentColor = Color.White
        )
      ) {
        Text(
          text = "Registrar",
          fontFamily = customFont,
          fontSize = 20.sp,
          modifier = Modifier.padding(10.dp)
        )
      }
      if (successMessage.isNotEmpty()) {
        Text(text = successMessage, color = Color.Green)
        LaunchedEffect(successMessage) {
          navController.navigate("login")
        }
      }
      if (errorMessage.isNotEmpty()) {
        Text(text = errorMessage, color = Color.Red)
      }
    }
    Row(
      modifier = Modifier.padding(bottom = 50.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Text(text = "¿Ya tienes cuenta?", fontFamily = customFont)
      Text(
        text = "Inicia sesión aquí",
        fontFamily = customFont,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable {
          navController.navigate("login")
        }
      )
    }
  }
}

// Retrofit Service
interface ApiServicePost {
  @POST("api/users/create")
  suspend fun registerUser(@Body user: RegisterRequest): RegisterResponse
}

// Retrofit Instance
object RetrofitInstancePost {
  private const val BASE_URL = "http://192.168.100.9:3000/" // Cambia según tu servidor

  val api: ApiServicePost by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiServicePost::class.java)
  }
}

// Modelo de solicitud
data class RegisterRequest(val name: String, val email: String, val password: String)

// Modelo de respuesta
data class RegisterResponse(val message: String)

// Función para hacer el POST
fun registerUser(
  name: String,
  email: String,
  password: String,
  onSuccess: () -> Unit,
  onError: (String) -> Unit
) {
  val request = RegisterRequest(name, email, password)
  val api = RetrofitInstancePost.api

  CoroutineScope(Dispatchers.IO).launch {
    try {
      val response = api.registerUser(request)
      withContext(Dispatchers.Main) {
        onSuccess()
      }
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        onError(e.message ?: "Error desconocido")
      }
    }
  }
}
