package cl.jlopezr.trivia.game.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton

@Composable
fun GameScreen(
    category: String,
    onBack: () -> Unit,
    viewModel: TriviaViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val feedback = viewModel.showFeedback // Observamos el estado de animación

    TriviaBackgroundContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- CAPA 1: EL JUEGO ---
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD: Puntos y Nivel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nivel: ${viewModel.currentLevel}", color = Color.Yellow, fontWeight = FontWeight.Bold)
                    Text("${viewModel.totalScore} Pts", color = Color.Cyan, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (val uiState = state) {
                    is TriviaUiState.Loading -> {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is TriviaUiState.Success -> {
                        val trivia = uiState.question
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = trivia.question,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            trivia.options.forEachIndexed { index, option ->
                                TriviaButton(
                                    text = option,
                                    onClick = {
                                        viewModel.processAnswer(
                                            isCorrect = index == trivia.correctIndex,
                                            category = category,
                                            onGameOver = onBack
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                    is TriviaUiState.Error -> {
                        Text("Error: ${uiState.message}", color = Color.Red)
                        TriviaButton(text = "REINTENTAR", onClick = { viewModel.loadQuestion(category) })
                    }
                }
            }

            // --- CAPA 2: ANIMACIÓN DE FUEGOS ARTIFICIALES / FEEDBACK ---
            AnimatedVisibility(
                visible = feedback != null,
                enter = fadeIn() + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut(targetScale = 1.5f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)), // Un poco más oscuro para resaltar el texto
                    contentAlignment = Alignment.Center
                ) {
                    when (feedback) {
                        FeedbackType.CORRECTO -> {
                            FeedbackContent(
                                title = "¡CORRECTO!",
                                color = Color.Green,
                                subTitle = "¡Vas por buen camino!"
                            )
                        }
                        FeedbackType.INCORRECTO -> {
                            FeedbackContent(
                                title = "¡INCORRECTO!",
                                color = Color.Red,
                                subTitle = "¡Vuelve a intentarlo!"
                            )
                        }
                        FeedbackType.SUBIO_NIVEL -> {
                            FeedbackContent(
                                title = "¡BIEN HECHO!",
                                color = Color.Yellow,
                                // Ahora muestra el nivel como número dinámicamente
                                subTitle = "Subiste al Nivel ${viewModel.currentLevel}"
                            )
                        }
                        null -> {}
                    }
                }
            }

        }
    }
}

@Composable
fun FeedbackContent(title: String, color: Color, subTitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        // Los emojis o iconos de fuegos artificiales
        Text(
            text = "✨ 🎆 ✨",
            fontSize = 60.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Título principal (¡CORRECTO! o ¡NIVEL COMPLETADO!)
        Text(
            text = title,
            color = color, // Aquí usará Verde o Amarillo según el caso
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(4f, 4f), blurRadius = 8f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // El texto de "Siguiente Nivel" ahora será Blanco y grande
        Text(
            text = subTitle,
            color = Color.White, // <--- CAMBIO: Forzamos Blanco para que se vea
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
            )
        )
    }
}
