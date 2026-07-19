package cl.jlopezr.trivia.ranking.presentation


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import cl.jlopezr.trivia.shared.core.data.UserSession
import org.jetbrains.compose.resources.stringResource
import myapplication.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(viewModel: RankingViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    val outlineStyle = TextStyle(
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 4f
        )
    )

    TriviaBackgroundContainer {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.ranking_title),
                            style = outlineStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Black)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back_desc),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                } else if (state.rankingList.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.no_ranking_data),
                        modifier = Modifier.align(Alignment.Center),
                        style = outlineStyle.copy(color = Color.Gray)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        itemsIndexed(state.rankingList) { index, item ->
                            RankingRow(index + 1, item, outlineStyle)
                        }
                    }
                }

                state.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center),
                        style = outlineStyle
                    )
                }
            }
        }
    }
}

@Composable
fun RankingRow(position: Int, item: RankingItem, outlineStyle: TextStyle) {
    // Comprobar si es el usuario actual (por username o email)
    val isCurrentUser = item.username == UserSession.username || item.username == UserSession.email

    val cardAlpha = if (isCurrentUser) 0.3f else 0.15f
    val borderColor = if (isCurrentUser) Color.Yellow else Color.White.copy(alpha = 0.2f)
    val borderWidth = if (isCurrentUser) 2.dp else 1.dp

    Surface(
        color = Color.White.copy(alpha = cardAlpha),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                style = outlineStyle.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (position <= 3) Color.Yellow else Color.White
                )
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.username,
                modifier = Modifier.weight(1f),
                style = outlineStyle.copy(
                    fontSize = 18.sp,
                    fontWeight = if (isCurrentUser) FontWeight.Black else FontWeight.Medium,
                    color = if (isCurrentUser) Color.Yellow else Color.White
                )
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.game_points_format, item.score),
                    style = outlineStyle.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                )
                Text(
                    text = stringResource(Res.string.level_format, item.level),
                    style = outlineStyle.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}