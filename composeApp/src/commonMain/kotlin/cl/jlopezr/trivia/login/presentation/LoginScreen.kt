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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val state: LoginUiState by viewModel.state.collectAsState()

    // Estilo común para todas las letras: Blancas con borde negro
    val outlineStyle = TextStyle(
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(0f, 0f),
            blurRadius = 8f
        )
    )

    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(Res.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        repeat(15) { ShootingStar() }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                modifier = Modifier.padding(8.dp).fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.login_title),
                        style = outlineStyle.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TextField Email
                    TextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text(stringResource(Res.string.email_label), style = outlineStyle.copy(fontSize = 14.sp)) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = outlineStyle,
                        colors = textFieldCustomColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // TextField Contraseña
                    TextField(
                        value = state.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = { Text(stringResource(Res.string.password_label), style = outlineStyle.copy(fontSize = 14.sp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = outlineStyle,
                        colors = textFieldCustomColors()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        LoginAnimatedButton(state, viewModel, outlineStyle)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Registro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = stringResource(Res.string.no_account_text), style = outlineStyle)
                        Text(
                            text = stringResource(Res.string.click_here),
                            style = outlineStyle.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Olvidé mi contraseña (Grande y con borde)
                    Text(
                        text = stringResource(Res.string.forgot_password_link),
                        modifier = Modifier.clickable { onNavigateToForgotPassword() },
                        style = outlineStyle.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun textFieldCustomColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.2f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = Color.White
)

@Composable
fun LoginAnimatedButton(state: LoginUiState, viewModel: LoginViewModel, textStyle: TextStyle) {
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
                text = stringResource(Res.string.btn_login),
                style = textStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Black)
            )
        }
    }
}

@Composable
fun ShootingStar() {
    val infiniteTransition = rememberInfiniteTransition()
    val startX = remember { Random.nextFloat() * 1000f }
    val startY = remember { Random.nextFloat() * 800f }
    val angle = remember { Random.nextFloat() * 360f }
    val speed = remember { Random.nextFloat() * 600f + 400f }
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
        val rad = (angle * (kotlin.math.PI / 180)).toFloat()
        val xOffset = progress * speed * kotlin.math.cos(rad.toDouble()).toFloat()
        val yOffset = progress * speed * kotlin.math.sin(rad.toDouble()).toFloat()

        Box(
            modifier = Modifier
                .offset(x = (startX + xOffset).dp, y = (startY + yOffset).dp)
                .size(width = 120.dp, height = 2.dp)
                .graphicsLayer(rotationZ = angle)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f))
                    )
                )
        )
    }
}

