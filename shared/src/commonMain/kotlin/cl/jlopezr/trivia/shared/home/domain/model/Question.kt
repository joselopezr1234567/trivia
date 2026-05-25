package cl.jlopezr.trivia.home.domain.model

data class Question(
    val id: String,
    val statement: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null
)