package cl.jlopezr.trivia.forgotpassword.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onPhoneChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onBack: () -> Unit
) {
    // Estilo con borde negro para coherencia visual
    val outlineStyle = TextStyle(
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(0f, 0f),
            blurRadius = 8f
        )
    )

    // Usamos tu nuevo contenedor que trae el fondo y las estrellas
    TriviaBackgroundContainer {
        Scaffold(
            containerColor = Color.Transparent, // Importante para ver el fondo y las estrellas
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Recuperar Cuenta",
                            style = outlineStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            // Usamos la ruta absoluta del icono para evitar errores de compilación en KMP
                            Icon(
                                imageVector = Icons.Default.ArrowBack, // <--- Simplificado
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Tarjeta de cristal (Glassmorphism)
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ingresa tu número de teléfono para recibir un código de verificación.",
                            style = outlineStyle.copy(fontSize = 16.sp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Campo de teléfono estilizado
                        TextField(
                            value = state.phoneNumber,
                            onValueChange = onPhoneChange,
                            label = { Text("Número de teléfono", style = outlineStyle.copy(fontSize = 14.sp)) },
                            placeholder = { Text("+56 9 XXXX XXXX", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                // Usamos la ruta absoluta del icono aquí también
                                Icon(
                                    imageVector = Icons.Default.Phone, // <--- Simplificado
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = state.errorMessage != null,
                            textStyle = outlineStyle,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.White
                            )
                        )

                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = Color.Red,
                                style = outlineStyle.copy(fontSize = 12.sp),
                                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Tu nuevo botón animado con la imagen btn_entrar
                        TriviaButton(
                            text = if (state.isLoading) "CARGANDO..." else "ENVIAR CÓDIGO",
                            onClick = onSendCode,
                            enabled = state.phoneNumber.isNotBlank() && !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = outlineStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Black)
                        )

                        if (state.isCodeSent) {
                            Text(
                                "¡Código enviado con éxito!",
                                modifier = Modifier.padding(top = 16.dp),
                                style = outlineStyle.copy(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}