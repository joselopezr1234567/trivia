package cl.jlopezr.trivia.game.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton

@Composable
fun GameScreen(
    category: String,
    difficulty: String,
    onBack: () -> Unit
) {
    TriviaBackgroundContainer {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Categoría: $category",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dificultad: $difficulty",
                color = Color.Yellow,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Aquí irá el loop de tus preguntas (QuestionCard)
            Text(
                text = "Cargando preguntas de la IA...",
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(100.dp))

            TriviaButton(
                text = "VOLVER",
                onClick = onBack
            )
        }
    }
}