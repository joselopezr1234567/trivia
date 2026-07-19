package cl.jlopezr.trivia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cl.jlopezr.trivia.home.presentation.HomeScreen
import cl.jlopezr.trivia.home.presentation.HomeViewModel
import cl.jlopezr.trivia.login.presentation.LoginScreen
import cl.jlopezr.trivia.registrer.presentation.RegisterScreen
import cl.jlopezr.trivia.game.presentation.GameScreen
import cl.jlopezr.trivia.game.presentation.TriviaViewModel
import cl.jlopezr.trivia.ranking.presentation.RankingScreen
import cl.jlopezr.trivia.ranking.presentation.RankingViewModel
import cl.jlopezr.trivia.login.presentation.LoginViewModel
import cl.jlopezr.trivia.registrer.presentation.RegisterViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 1. Pantalla de Login
        composable("login") {
            val loginViewModel: LoginViewModel = koinViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        // 2. Pantalla: Olvidé Contraseña
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

        // 3. Pantalla: Reset de Contraseña
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
            val registerViewModel: RegisterViewModel = koinViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // 5. Home
        composable("home") {
            val homeViewModel: HomeViewModel = koinViewModel()

            LaunchedEffect(Unit) {
                homeViewModel.loadUserProgress()
                homeViewModel.syncFromCloud()
            }

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRanking = { navController.navigate("ranking") },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onGenerateQuestions = { category, difficulty ->
                    navController.navigate("game/$category/$difficulty")
                }
            )
        }

        // 6. Game Screen
        composable(
            route = "game/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "General"
            val gameViewModel: TriviaViewModel = koinViewModel()

            GameScreen(
                category = category,
                onBack = { navController.popBackStack() },
                viewModel = gameViewModel
            )
        }

        // 7. Ranking
        composable("ranking") {
            val rankingViewModel: RankingViewModel = koinViewModel()
            RankingScreen(
                viewModel = rankingViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
