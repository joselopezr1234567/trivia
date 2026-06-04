package cl.jlopezr.trivia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.jlopezr.trivia.forgotpassword.data.ForgotPasswordRepositoryImpl
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ResetPasswordUseCase
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ValidatePhoneUseCase
import cl.jlopezr.trivia.forgotpassword.presentation.ForgotPasswordScreen
import cl.jlopezr.trivia.forgotpassword.presentation.ForgotPasswordViewModel
import cl.jlopezr.trivia.forgotpassword.presentation.ResetPasswordScreen
import cl.jlopezr.trivia.home.data.repository.TriviaRepositoryImpl
import cl.jlopezr.trivia.home.domain.usecase.GetQuestionsUseCase
import cl.jlopezr.trivia.home.presentation.HomeScreen
import cl.jlopezr.trivia.home.presentation.HomeViewModel
import cl.jlopezr.trivia.login.presentation.LoginScreen
import cl.jlopezr.trivia.registrer.presentation.RegisterScreen
import cl.jlopezr.trivia.game.presentation.GameScreen
import cl.jlopezr.trivia.game.presentation.TriviaViewModel
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 1. Pantalla de Login
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        // 2. Pantalla: Olvidé Contraseña (Ingresar Teléfono)
        composable("forgot_password") {
            val repository = ForgotPasswordRepositoryImpl()
            val validateUseCase = ValidatePhoneUseCase(repository)
            val resetUseCase = ResetPasswordUseCase(repository)

            val viewModel = viewModel<ForgotPasswordViewModel> {
                ForgotPasswordViewModel(validateUseCase, resetUseCase)
            }

            LaunchedEffect(viewModel.state.isCodeSent) {
                if (viewModel.state.isCodeSent) {
                    navController.navigate("reset_password/${viewModel.state.phoneNumber}")
                }
            }

            ForgotPasswordScreen(
                state = viewModel.state,
                onPhoneChange = viewModel::onPhoneChange,
                onSendCode = { viewModel.sendCode() },
                onBack = { navController.popBackStack() }
            )
        }

        // 3. Pantalla: Reset de Contraseña (Validar código y nueva clave)
        composable(
            route = "reset_password/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val repository = ForgotPasswordRepositoryImpl()
            val validateUseCase = ValidatePhoneUseCase(repository)
            val resetUseCase = ResetPasswordUseCase(repository)

            val viewModel = viewModel<ForgotPasswordViewModel> {
                ForgotPasswordViewModel(validateUseCase, resetUseCase)
            }

            LaunchedEffect(viewModel.resetState.isSuccess) {
                if (viewModel.resetState.isSuccess) {
                    navController.navigate("login") {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            }

            ResetPasswordScreen(
                state = viewModel.resetState,
                onCodeChange = viewModel::onCodeChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                onResetPassword = { viewModel.resetPassword(phone) },
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Registro
        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }

        // 5. HOME (Pantalla Principal con Categorías y Puntos)
        composable("home") {
            val repository = TriviaRepositoryImpl()
            val getQuestionsUseCase = GetQuestionsUseCase(repository)

            val homeViewModel = viewModel<HomeViewModel> {
                HomeViewModel(getQuestionsUseCase)
            }

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRanking = { navController.navigate("ranking") },
                onGenerateQuestions = { category, difficulty ->
                    // Navegamos al juego pasando la categoría y dificultad inicial
                    navController.navigate("game/$category/$difficulty")
                }
            )
        }

        // 6. GAME SCREEN (Donde ocurre la trivia y las animaciones)
        composable(
            route = "game/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "General"

            // Repositorio del módulo shared
            val triviaRepository = TriviaRepository()

            // Creamos el ViewModel con el tipo genérico explícito para evitar errores de tipo
            val gameViewModel = viewModel<TriviaViewModel> {
                TriviaViewModel(triviaRepository)
            }

            // Lanzamos la carga de la primera pregunta al entrar a la pantalla
            LaunchedEffect(category) {
                gameViewModel.loadQuestion(category)
            }

            GameScreen(
                category = category,
                onBack = {
                    // Al volver al Home, los puntos deberían estar actualizados
                    navController.popBackStack()
                },
                viewModel = gameViewModel
            )
        }

        // 7. RANKING
        composable("ranking") {
            // Aquí iría tu pantalla de RankingScreen(onBack = { navController.popBackStack() })
        }
    }
}