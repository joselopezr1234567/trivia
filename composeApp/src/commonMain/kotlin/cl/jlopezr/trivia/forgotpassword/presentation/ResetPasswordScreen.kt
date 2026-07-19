package cl.jlopezr.trivia.forgotpassword.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton
import org.jetbrains.compose.resources.stringResource
import myapplication.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    state: ResetPasswordState,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onResetPassword: () -> Unit,
    onBack: () -> Unit
) {
    // Estilo con borde negro (Shadow) para todas las letras
    val outlineStyle = TextStyle(
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(0f, 0f),
            blurRadius = 8f
        )
    )

    TriviaBackgroundContainer {
        Scaffold(
            containerColor = Color.Transparent, // Para ver el fondo y las estrellas
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.reset_password_title),
                            style = outlineStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.back_desc), tint = Color.White)
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
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.reset_password_instr),
                            style = outlineStyle.copy(fontSize = 15.sp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Campo Código
                        CustomResetField(
                            value = state.code,
                            onValueChange = { if (it.length <= 6) onCodeChange(it) },
                            label = stringResource(Res.string.code_field_label),
                            icon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Number,
                            outlineStyle = outlineStyle
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Nueva Contraseña
                        CustomResetField(
                            value = state.newPassword,
                            onValueChange = onPasswordChange,
                            label = stringResource(Res.string.new_password_label),
                            icon = Icons.Default.VpnKey,
                            keyboardType = KeyboardType.Password,
                            outlineStyle = outlineStyle,
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    val icon = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    Icon(icon, contentDescription = null, tint = Color.White)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Confirmar Contraseña
                        CustomResetField(
                            value = state.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = stringResource(Res.string.confirm_password_label),
                            icon = Icons.Default.CheckCircle,
                            keyboardType = KeyboardType.Password,
                            outlineStyle = outlineStyle,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = state.newPassword != state.confirmPassword && state.confirmPassword.isNotEmpty()
                        )

                        if (state.newPassword != state.confirmPassword && state.confirmPassword.isNotEmpty()) {
                            Text(
                                stringResource(Res.string.passwords_dont_match),
                                color = Color.Red,
                                style = outlineStyle.copy(fontSize = 12.sp),
                                modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                            )
                        }

                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = Color.Red,
                                style = outlineStyle.copy(fontSize = 12.sp),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón con imagen btn_entrar
                        TriviaButton(
                            text = if (state.isLoading) "..." else stringResource(Res.string.btn_update_password),
                            onClick = onResetPassword,
                            enabled = state.code.length == 6 &&
                                    state.newPassword.isNotEmpty() &&
                                    state.newPassword == state.confirmPassword &&
                                    !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = outlineStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para mantener los TextFields consistentes y limpios
 */
@Composable
fun CustomResetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    outlineStyle: TextStyle,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = outlineStyle.copy(fontSize = 14.sp)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.White) },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        isError = isError,
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
}