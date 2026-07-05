package cl.jlopezr.trivia.ranking.presentation

data class RankingUiState(
    val rankingList: List<RankingItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)