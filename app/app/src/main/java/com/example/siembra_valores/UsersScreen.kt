package com.example.siembra_valores

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Modelo de datos
data class User(
  val id: String,
  val name: String,
  val email: String
)

data class Plant(
  val id: String,
  val name: String,
  val type: String,
  val description: String,
  val historiales: List<History>
)

data class CreatePlantRequest(
  val name: String,
  val usuario_id: String
)

// Model para los servicios seleccionados
data class AddServicesRequest(
  val plantaId: String,
  val servicioIds: List<String>
)

// Model para la respuesta de los servicios
data class Service(
  val id: String,
  val name: String,
  val description: String
)

data class ServiceResponse(
  val services: List<Service>
)

// Interfaz de Retrofit
interface ApiService {
  @GET("api/services")
  suspend fun getServices(): List<Service>

  @GET("api/users/get/{id}")
  suspend fun getUserById(@Path("id") id: String): User

  @GET("api/plants/user/{userId}")
  suspend fun getPlantsByUserId(@Path("userId") userId: String): List<Plant>

  @GET("api/plants/{id}")
  suspend fun getPlantById(@Path("id") id: String): PlantWithHistory

  @POST("api/plants/create")
  suspend fun createPlant(@Body plant: CreatePlantRequest): Plant

  @DELETE("api/plants/delete/{id}")
  suspend fun deletePlant(@Path("id") plantId: String): Response<Unit>

  @POST("api/plants/add-services")
  suspend fun addServicesToPlant(
    @Body request: AddServicesRequest
  ): Response<Unit>
}

// Instancia de Retrofit
object RetrofitInstance {
  private const val BASE_URL =
    "http://192.168.100.9:3000/" // Cambia por la dirección base de tu servidor

  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}

@Composable
fun UserDetailsScreen(navController: NavController, context: Context) {
  val token = getToken(context)
  var user by remember { mutableStateOf<User?>(null) }
  var plants by remember { mutableStateOf<List<Plant>>(emptyList()) }
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
  var name by remember { mutableStateOf("") }

  if (token != null) {
    val userId = getUserIdFromToken(token)  // Método para obtener el userId desde el token

    LaunchedEffect(Unit) {
      coroutineScope.launch {
        try {
          // Obtener datos del usuario
          val userResponse = RetrofitInstance.api.getUserById(userId)
          user = userResponse

          // Obtener las plantas del usuario
          val plantsResponse = RetrofitInstance.api.getPlantsByUserId(userId)
          plants = plantsResponse
        } catch (e: Exception) {
          errorMessage = e.message ?: "Error al obtener los datos"
        }
      }
    }
  } else {
    errorMessage = "No se encontró el token JWT"
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp)
      .systemBarsPadding()
  ) {
    user?.let {
      Column(modifier = Modifier.padding(bottom = 30.dp)) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(0.dp),
          modifier = Modifier
            .padding(bottom = 30.dp)
            .fillMaxWidth()
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
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(0.dp),
          modifier = Modifier
            .padding(bottom = 20.dp)
            .fillMaxWidth()
        ) {
          Text(
            text = it.name,
            fontFamily = customFont,
            fontWeight = FontWeight.Medium,
            fontSize = 30.sp
          )
          Text(
            text = "ID de usuario: ${it.id}",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = monoFont,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Nombre de la planta") }
          )
          Button(
            onClick = {
              coroutineScope.launch {
                if (name.isNotEmpty() && token != null) {
                  val userId = getUserIdFromToken(token)
                  try {
                    val newPlant = RetrofitInstance.api.createPlant(
                      CreatePlantRequest(name, usuario_id = userId)
                    )

                    // Actualiza el estado de `plants` para que la UI se recargue
                    //plants = plants + newPlant

                    name = ""  // Limpiar el campo de texto

                    // Navegar hacia la misma pantalla y luego regresar para forzar una recarga
                    //navController.popBackStack()  // Regresar a la pantalla anterior
                    navController.navigate("users")  // Volver a navegar hacia la misma pantalla

                  } catch (e: Exception) {
                    errorMessage = e.message ?: "Error al agregar la planta"
                  }
                } else {
                  errorMessage = "Por favor ingresa un nombre para la planta"
                }
              }
            },
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Black,
              contentColor = Color.White
            )
          ) {
            Text(text = "Agregar")
          }
        }
        if (errorMessage.isNotEmpty()) {
          Text(
            text = errorMessage,
            color = Color.Red,
            modifier = Modifier.padding(top = 10.dp),
            fontStyle = FontStyle.Italic
          )
        }
      }

      if (plants.isNotEmpty()) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(plants) { plant ->
            Column(modifier = Modifier.clickable { navController.navigate("plantDetails/${plant.id}") }) {
              Text(
                text = plant.name,
                fontFamily = customFont,
                fontSize = 40.sp,
                modifier = Modifier.fillMaxWidth()
              )
              Text(
                text = "ID de planta: ${plant.id}",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 30.dp),
                fontSize = 10.sp,
                fontFamily = monoFont,
              )
            }
          }
        }
      } else {
        Text(text = "No tienes plantas asociadas", fontStyle = FontStyle.Italic)
      }
    }
  }
  }


  // Función para obtener el userId del token
fun getUserIdFromToken(token: String): String {
  try {
    val jwt = JWT(token)
    return jwt.getClaim("id").asString() ?: ""  // Aquí extraemos el campo "id" del token
  } catch (e: Exception) {
    return ""  // Si ocurre un error en la decodificación del token, retorna un valor vacío
  }
}