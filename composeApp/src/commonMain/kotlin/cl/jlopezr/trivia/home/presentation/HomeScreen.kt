package cl.jlopezr.trivia.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jlopezr.trivia.core.components.TriviaBackgroundContainer
import cl.jlopezr.trivia.core.components.TriviaButton
import cl.jlopezr.trivia.shared.core.data.UserSession
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import myapplication.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRanking: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGenerateQuestions: (category: String, difficulty: String) -> Unit,
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val difficulties = listOf(
        stringResource(Res.string.difficulty_easy),
        stringResource(Res.string.difficulty_medium),
        stringResource(Res.string.difficulty_hard)
    )

    val outlineStyle = TextStyle(
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 4f
        )
    )

    val placeholderStyle = TextStyle(
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(3f, 3f),
            blurRadius = 2f
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier.width(280.dp),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                drawerTonalElevation = 0.dp
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = stringResource(Res.string.menu_title),
                    style = outlineStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(16.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Yellow) },
                    label = { Text(stringResource(Res.string.btn_ranking), style = outlineStyle.copy(fontSize = 16.sp)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToRanking()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color.Cyan) },
                    label = { Text(stringResource(Res.string.btn_invite_friends), style = outlineStyle.copy(fontSize = 16.sp)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.inviteFriends()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(8.dp)
                )

                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            imageVector = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp, 
                            contentDescription = null, 
                            tint = if (state.isMuted) Color.Gray else Color.Green
                        ) 
                    },
                    label = { 
                        Text(
                            text = if (state.isMuted) stringResource(Res.string.sound_on) else stringResource(Res.string.sound_off), 
                            style = outlineStyle.copy(fontSize = 16.sp)
                        ) 
                    },
                    selected = false,
                    onClick = {
                        viewModel.toggleMute()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red) },
                    label = { Text(stringResource(Res.string.btn_logout), style = outlineStyle.copy(fontSize = 16.sp, color = Color.Red)) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            // Limpiar sesión
                            UserSession.email = ""
                            UserSession.username = ""
                            onNavigateToLogin()
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) {
        TriviaBackgroundContainer {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "TRIV-IA",
                                style = outlineStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Black)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp)) // Ajustado un poco hacia arriba

                    Text(
                        text = "",
                        style = outlineStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    )

                    // --- NUEVO PANEL DE PROGRESO PERSISTENTE ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(Res.string.progress_label),
                                        style = outlineStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Light)
                                    )
                                }
                                Text(
                                    // Agregamos "Nivel" antes del número para que sea más claro
                                    text = stringResource(Res.string.level_format, state.currentLevel),
                                    style = outlineStyle.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Yellow
                                    )
                                )
                            }

                            VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(40.dp))

                            // Mostrar Puntos
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(Res.string.points_label), style = outlineStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Light))
                                }
                                Text(
                                    text = "${state.totalScore}",
                                    style = outlineStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Cyan)
                                )
                            }

                            VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(40.dp))

                            // --- NUEVO: PREMIO ---
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color.Green,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(Res.string.prize_label),
                                        style = outlineStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Light)
                                    )
                                }
                                Text(
                                    text = stringResource(Res.string.game_prize_format, ((state.totalEarnings * 1000).toInt() / 1000.0).toString()),
                                    style = outlineStyle.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Green
                                    )
                                )
                            }
                        }
                    }
                    // -------------------------------------------

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(Res.string.choose_category),
                        style = outlineStyle.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // INPUT CON EFECTO DE PROFUNDIDAD
                    Box(modifier = Modifier.fillMaxWidth().height(65.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = 4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TextField(
                                value = state.category,
                                onValueChange = { viewModel.onCategoryChanged(it) },
                                placeholder = {
                                    Text(stringResource(Res.string.category_placeholder), style = placeholderStyle)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = outlineStyle.copy(fontSize = 20.sp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage!!,
                            color = Color.Red,
                            style = outlineStyle.copy(fontSize = 14.sp),
                            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp, start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = stringResource(Res.string.difficulty_label),
                        style = outlineStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(difficulties) { diff ->
                            val isSelected = state.difficulty == diff
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onDifficultySelected(diff) },
                                label = {
                                    Text(
                                        text = diff,
                                        style = if (isSelected)
                                            TextStyle(fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        else outlineStyle.copy(fontSize = 16.sp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.3f),
                                    selectedBorderColor = Color.White,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 2.dp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        TriviaButton(
                            text = stringResource(Res.string.btn_play_now),
                            enabled = state.category.isNotBlank() && !state.isLoading,
                            onClick = {
                                viewModel.generateTrivia { cat: String, diff: String ->
                                    onGenerateQuestions(cat, diff)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textStyle = outlineStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}