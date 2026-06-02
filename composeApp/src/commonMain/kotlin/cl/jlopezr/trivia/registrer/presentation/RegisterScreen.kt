package cl.jlopezr.trivia.registrer.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.registrer.presentation.RegisterViewModel
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.btn_entrar
import myapplication.composeapp.generated.resources.fondo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel // ✅ Necesario para Koin
import kotlin.math.*
import kotlin.random.Random

// --- Modelo para las Estrellas Fugaces (Mantenido) ---
data class ShootingStar(
    val id: Int,
    val xStart: Float,
    val yStart: Float,
    val angle: Float,
    val length: Float,
    val speed: Float,
    val delay: Long,
    val duration: Long
)

@Composable
fun ShootingStarsBackground() {
    val numberOfStars = 6
    val infiniteTransition = rememberInfiniteTransition(label = "StarsLoop")
    val elapsedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Timer"
    )

    val stars = remember {
        List(numberOfStars) { i ->
            ShootingStar(
                id = i,
                xStart = Random.nextFloat() * 1200f,
                yStart = Random.nextFloat() * 800f - 400f,
                angle = (PI / 4 + (Random.nextFloat() - 0.5f) * PI / 8).toFloat(),
                length = 180f + Random.nextFloat() * 200f,
                speed = 0.7f + Random.nextFloat() * 1.2f,
                delay = Random.nextLong(0, 4000),
                duration = 700L + Random.nextLong(0, 1000L)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            if (elapsedTime in star.delay.toFloat()..(star.delay + star.duration).toFloat()) {
                val progress = (elapsedTime - star.delay) / star.duration
                val distance = star.speed * (elapsedTime - star.delay)
                val currentX = star.xStart + distance * cos(star.angle.toDouble()).toFloat()
                val currentY = star.yStart + distance * sin(star.angle.toDouble()).toFloat()
                val tailX = currentX - star.length * cos(star.angle.toDouble()).toFloat() * progress
                val tailY = currentY - star.length * sin(star.angle.toDouble()).toFloat() * progress
                val alpha = if (progress < 0.2f) progress / 0.2f else (1f - progress).coerceAtLeast(0f)

                drawLine(
                    color = Color.White.copy(alpha = alpha * 0.6f),
                    start = Offset(tailX, tailY),
                    end = Offset(currentX, currentY),
                    strokeWidth = 4f
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    center = Offset(currentX, currentY),
                    radius = 3f
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel() // ✅ Inyectado con Koin
) {
    // Estados del formulario
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Observar estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // ✅ Efecto para navegar cuando el registro sea exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            println("🚀 REGISTRO EXITOSO: Navegando al Login...")
            onNavigateToLogin()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonPressedOffset by animateFloatAsState(
        targetValue = if (isPressed) 6f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        ShootingStarsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crea tu Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomRegisterField(value = fullName, onValueChange = { fullName = it }, label = "Nombre Completo")
            CustomRegisterField(value = phone, onValueChange = { phone = it }, label = "Teléfono", isPhone = true)
            CustomRegisterField(value = email, onValueChange = { email = it }, label = "Email")
            CustomRegisterField(value = password, onValueChange = { password = it }, label = "Contraseña", isPassword = true)
            CustomRegisterField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirmar Contraseña", isPassword = true)

            // Mensaje de error si las contraseñas no coinciden
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text("Las contraseñas no coinciden", color = Color.Yellow, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ BOTÓN DE REGISTRO CONECTADO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .graphicsLayer { translationY = buttonPressedOffset.dp.toPx() }
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !uiState.isLoading // Deshabilitar si está cargando
                    ) {
                        println("🔵 Click en Registrarme")
                        if (email.isNotEmpty() && password == confirmPassword) {
                            // ✅ LLAMADA AL VIEWMODEL
                            viewModel.register(
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                password = password
                            )
                        } else if (email.isEmpty()) {
                            println("⚠️ Error: Email vacío")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.btn_entrar),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Registrarme",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ✅ Mostrar error del servidor si existe
            uiState.error?.let { errorMsg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMsg, color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomRegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isPhone: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontWeight = FontWeight.Bold) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password
            else if (isPhone) KeyboardType.Phone
            else KeyboardType.Email
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            cursorColor = Color.White
        )
    )
}