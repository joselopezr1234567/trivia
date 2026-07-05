package cl.jlopezr.trivia.ranking.presentation

import kotlinx.serialization.Serializable

@Serializable
data class RankingItem(
    val username: String,
    val score: Int,
    val level: Int,
    val position: Int,
    val avatarUrl: String? = null
)