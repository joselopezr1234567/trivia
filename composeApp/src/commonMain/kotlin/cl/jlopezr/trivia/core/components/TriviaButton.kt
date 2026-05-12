package cl.jlopezr.trivia.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TriviaButton(
    resource: DrawableResource,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    // Esto detecta si el usuario está tocando la pantalla
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Si está presionado, se achica a 0.95, si no, vuelve a 1.0
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(width = 220.dp, height = 70.dp) // Tamaño estándar para toda tu app
            .graphicsLayer(scaleX = scale, scaleY = scale) // Aplicamos el efecto de escala
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Quitamos el círculo gris feo de Android
                enabled = enabled,
                onClick = onClick
            )
    )
}