package cl.jlopezr.trivia.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.jlopezr.trivia.login.presentation.LoginScreen
import cl.jlopezr.trivia.register.presentation.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) { // <--- Inicia el bloque del NavHost

        // 1. Pantalla de Login
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }, // ✅ Coma necesaria
                onNavigateToRegister = { // ✅ Agregado el "=" y corregida la sintaxis
                    navController.navigate("register")
                }
            )
        }

        // 2. Pantalla de Registro
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Pantalla Home
        composable("home") {
            androidx.compose.material3.Text("¡Bienvenido a la Trivia!")
        }

    } // <--- Aquí es donde DEBE cerrar el NavHost
}