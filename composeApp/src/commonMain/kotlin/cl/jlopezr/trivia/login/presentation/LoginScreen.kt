package cl.jlopezr.trivia.login.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.jlopezr.trivia.login.domain.AuthRepository
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.btn_entrar
import myapplication.composeapp.generated.resources.fondo

import org.jetbrains.compose.resources.painterResource
// Nota: Si btn_entrar o fondo dan error, recuerda que se acceden como Res.drawable.nombre

import kotlin.random.Random
import kotlin.math.*
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel<LoginViewModel> { LoginViewModel(authRepository = DummyAuthRepository()) },
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val state: LoginUiState by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. IMAGEN DE FONDO
        Image(
            painter = painterResource(Res.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. ESTRELLAS FUGACES (Ahora son 15 estrellas en todas direcciones)
        repeat(15) {
            ShootingStar()
        }

        // 3. CONTENIDO (Vidrio y formulario)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text("Email", color = Color.Black.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = state.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = { Text("Contraseña", color = Color.Black.copy(alpha = 0.6f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val buttonOffset by animateDpAsState(if (isPressed) 6.dp else (-4).dp)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().offset(y = 8.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().offset(y = buttonOffset)
                                    .clip(RoundedCornerShape(30.dp))
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                                        onClick = { viewModel.login() }
                                    )
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.btn_entrar),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )
                                Text(
                                    text = "ENTRAR",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "¿No tienes cuenta? ", color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = "Haz clic aquí",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToRegister()}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShootingStar() {
    val infiniteTransition = rememberInfiniteTransition()

    // Parámetros aleatorios únicos por estrella
    val startX = remember { Random.nextFloat() * 1000f }
    val startY = remember { Random.nextFloat() * 800f }
    val angle = remember { Random.nextFloat() * 360f } // Dirección aleatoria (0 a 360 grados)
    val speed = remember { Random.nextFloat() * 600f + 400f } // Velocidad aleatoria
    val duration = remember { Random.nextInt(1500, 4000) }
    val delay = remember { Random.nextInt(0, 8000) }

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
        )
    )

    if (progress > 0f) {
        // Cálculo matemático para mover en cualquier dirección basado en el ángulo
        val rad = (angle * (kotlin.math.PI / 180)).toFloat()
        val xOffset = progress * speed * kotlin.math.cos(rad.toDouble()).toFloat()
        val yOffset = progress * speed * kotlin.math.sin(rad.toDouble()).toFloat()

        Box(
            modifier = Modifier
                .offset(x = (startX + xOffset).dp, y = (startY + yOffset).dp)
                .size(width = 120.dp, height = 2.dp)
                .graphicsLayer(rotationZ = angle) // La estrella apunta hacia donde viaja
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f))
                    )
                )
        )
    }
}

class DummyAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        kotlinx.coroutines.delay(2000)
        return Result.success(Unit)
    }
}