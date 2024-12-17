package com.example.siembra_valores

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ChangeStatusBarColor(
        statusBarColor = Color.Transparent,
        navigationBarColor = Color.Transparent,
        isLightIcons = false
      )
      App()
    }
  }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
  val navController = rememberNavController()
  val context = LocalContext.current
  NavHost(navController = navController, startDestination = "login") {
    composable("login") { LoginScreen(navController, context) }
    composable("register") { RegisterScreen(navController) }
    composable("users") { UserDetailsScreen(navController, context) }
    // Ruta para los detalles de la planta
    composable("plantDetails/{plantId}") { backStackEntry ->
      val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
      PlantScreen(navController, plantId)
    }
    composable("plant_service_screen/{plantId}") { backStackEntry ->
      val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
      ServicesScreen(navController, plantId)
    }
  }
}

