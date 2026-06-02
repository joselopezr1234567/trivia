package cl.jlopezr.trivia.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.btn_entrar
import myapplication.composeapp.generated.resources.fondo
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource

/**
 * Contenedor principal que centraliza el fondo y el efecto visual de estrellas.
 * Evita duplicados en otras pantallas.
 */
@Composable
fun TriviaBackgroundContainer(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(Res.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Capa de estrellas fugaces (15 instancias independientes)
        repeat(15) {
            ShootingStar()
        }

        // El contenido de la pantalla (Scaffold, Column, etc.)
        content()
    }
}

/**
 * Botón personalizado con estética JLdeveloper.
 * Incluye estados de habilitado/deshabilitado y animación de presión.
 */
@Composable
fun TriviaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true, // Solución al error 'No value passed'
    textStyle: TextStyle = TextStyle(
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animación de desplazamiento al presionar
    val buttonOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 6.dp else 0.dp,
        label = "buttonOffset"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(80.dp)
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                // Sutil transparencia cuando está deshabilitado
                alpha = if (enabled) 1f else 0.6f
            }
    ) {
        // Sombra / Profundidad del botón
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Cuerpo del botón con imagen btn_entrar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = buttonOffset)
                .clip(RoundedCornerShape(30.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
        ) {
            Image(
                painter = painterResource(Res.drawable.btn_entrar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = text,
                style = textStyle
            )
        }
    }
}

/**
 * Componente visual que genera estrellas con trayectorias aleatorias.
 */
@Composable
fun ShootingStar() {
    val infiniteTransition = rememberInfiniteTransition(label = "starTransition")

    // Parámetros de vuelo aleatorios
    val startX = remember { Random.nextFloat() * 1000f }
    val startY = remember { Random.nextFloat() * 1000f }
    val angle = remember { Random.nextFloat() * 45f + 135f }
    val speed = remember { Random.nextFloat() * 700f + 500f }
    val duration = remember { Random.nextInt(1200, 2800) }
    val delay = remember { Random.nextInt(0, 4000) }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration + delay
                0f at delay
                1f at duration + delay
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "starProgress"
    )

    if (progress > 0f) {
        val rad = (angle * (kotlin.math.PI / 180)).toFloat()
        val xOffset = progress * speed * kotlin.math.cos(rad.toDouble()).toFloat()
        val yOffset = progress * speed * kotlin.math.sin(rad.toDouble()).toFloat()

        Box(
            modifier = Modifier
                .offset(x = (startX + xOffset).dp, y = (startY + yOffset).dp)
                .size(width = 120.dp, height = 1.dp)
                .graphicsLayer(rotationZ = angle)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.7f))
                    )
                )
        )
    }
}