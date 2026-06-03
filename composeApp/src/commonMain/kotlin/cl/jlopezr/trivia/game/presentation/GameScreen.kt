package cl.jlopezr.trivia.game.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository


@Composable
fun GameScreen(
    category: String,
    difficulty: String,
    onBack: () -> Unit,
    // Inicializamos el ViewModel aquí mismo para probar
    viewModel: TriviaViewModel = remember { TriviaViewModel(TriviaRepository()) }
) {
    // Observamos el estado del Flow (Loading, Success o Error)
    val state by viewModel.uiState.collectAsState()

    // Disparamos la carga de la pregunta SOLO UNA VEZ al entrar
    LaunchedEffect(Unit) {
        viewModel.loadQuestion("$category ($difficulty)")
    }

    TriviaBackgroundContainer {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CABECERA
            Text(
                text = category,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // MANEJO DE ESTADOS
            when (val uiState = state) {
                is TriviaUiState.Loading -> {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("La IA está pensando...", color = Color.White)
                    }
                }

                is TriviaUiState.Error -> {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        Text("Error: ${uiState.message}", color = Color.Red, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        TriviaButton(text = "REINTENTAR", onClick = { viewModel.loadQuestion(category) })
                    }
                }

                is TriviaUiState.Success -> {
                    val trivia = uiState.question

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // PREGUNTA
                        Text(
                            text = trivia.question,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // OPCIONES
                        trivia.options.forEachIndexed { index, option ->
                            TriviaButton(
                                text = option,
                                onClick = {
                                    // Aquí puedes añadir la lógica de verificar si es correcta
                                    // Por ahora, solo cargamos otra para probar el flujo
                                    viewModel.loadQuestion(category)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // BOTÓN SIEMPRE VISIBLE PARA SALIR
            TriviaButton(
                text = "SALIR",
                onClick = onBack,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}