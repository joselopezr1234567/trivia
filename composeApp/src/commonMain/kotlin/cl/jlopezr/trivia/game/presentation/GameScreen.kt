package cl.jlopezr.trivia.game.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import cl.jlopezr.trivia.core.ads.getAdsManager
import cl.jlopezr.trivia.core.audio.getAudioManager
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton
import cl.jlopezr.trivia.shared.core.data.ProgressStorage


@Composable
fun GameScreen(
    category: String,
    onBack: () -> Unit,
    viewModel: TriviaViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val feedback = viewModel.showFeedback // Observamos el estado de animación

    LaunchedEffect(category) {
        viewModel.loadQuestion(category, onGameOver = onBack)
    }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Nivel: ${viewModel.currentLevel}", color = Color.Yellow, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Premio: $${(ProgressStorage.totalEarnings * 1000).toInt() / 1000.0}",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    }

                    // --- RELOJ ---
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = viewModel.timeLeft / 10f,
                            color = if (viewModel.timeLeft <= 3) Color.Red else Color.Cyan,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "${viewModel.timeLeft}",
                            color = if (viewModel.timeLeft <= 3) Color.Red else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Text("${viewModel.totalScore} Pts", color = Color.Cyan, fontWeight = FontWeight.Black)

                    IconButton(onClick = { viewModel.toggleMute() }) {
                        Icon(
                            imageVector = if (viewModel.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = Color.White
                        )
                    }
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
                                    enabled = !viewModel.isAnswerSelected, // DESHABILITAR SI YA ELIGIÓ
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
                        TriviaButton(
                            text = "REINTENTAR",
                            onClick = { viewModel.loadQuestion(category, onGameOver = onBack) }
                        )
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
                                subTitle = "La respuesta correcta era: ${ (state as? TriviaUiState.Success)?.question?.let { it.options[it.correctIndex] } }\n\n${ (state as? TriviaUiState.Success)?.question?.explanation }"
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
                        FeedbackType.TIEMPO_AGOTADO -> {
                            FeedbackContent(
                                title = "¡TIEMPO AGOTADO!",
                                color = Color.Red,
                                subTitle = "La respuesta correcta era: ${ (state as? TriviaUiState.Success)?.question?.let { it.options[it.correctIndex] } }\n\n¡Inténtalo nuevamente!"
                            )
                        }
                        null -> {}
                    }
                }
            }

            // --- CAPA 3: PROMPT DE VIDEO RECOMPENSADO ---
            if (viewModel.showRewardedPrompt) {
                val outlineStyle = TextStyle(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { /* Bloquear clics al fondo */ },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color.Yellow)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✨ BONUS EXTRA ✨",
                                style = outlineStyle.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Yellow
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "¿Quieres ganar 50 PUNTOS EXTRAS viendo un video corto?",
                                style = outlineStyle.copy(fontSize = 18.sp),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            TriviaButton(
                                text = "¡SÍ, VER VIDEO!",
                                onClick = {
                                    viewModel.dismissRewardedPrompt()
                                    getAdsManager().showRewarded(
                                        onRewardEarned = { amount ->
                                            viewModel.addBonusPoints(50)
                                        },
                                        onAdClosed = { }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = { viewModel.dismissRewardedPrompt() }) {
                                Text(
                                    text = "AHORA NO, GRACIAS",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
