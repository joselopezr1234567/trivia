package cl.jlopezr.trivia.server

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// 🕒 Función de utilidad para generar el "Ahora" compatible con Exposed en tu zona horaria
fun currentKotlinDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

// ==========================================
// 👤 CAPA DE USUARIOS Y AUTENTICACIÓN
// ==========================================

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = text("password")
    val phone = varchar("phone", 20).uniqueIndex().nullable()
    val verificationCode = varchar("verification_code", 6).default("000000")

    override val primaryKey = PrimaryKey(id)
}

object UserPointsTable : Table("user_points") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()
    val totalPoints = integer("total_points").default(0)
    val updatedAt = datetime("updated_at").default(currentKotlinDateTime()) // ✅ Corregido

    override val primaryKey = PrimaryKey(id)
}

object UserLevelsTable : Table("user_levels") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()
    val currentLevel = integer("current_level").default(1)
    val currentExperience = integer("current_experience").default(0)
    val updatedAt = datetime("updated_at").default(currentKotlinDateTime()) // ✅ Corregido

    override val primaryKey = PrimaryKey(id)
}

// ==========================================
// 🧠 CAPA DE TRIVIA E INTELIGENCIA ARTIFICIAL
// ==========================================

object QuestionsTable : Table("questions") {
    val id = integer("id").autoIncrement()
    val category = varchar("category", 100)
    val questionText = text("question_text")
    val optionA = varchar("option_a", 255)
    val optionB = varchar("option_b", 255)
    val optionC = varchar("option_c", 255)
    val optionD = varchar("option_d", 255)
    val correctOption = char("correct_option") // 'A', 'B', 'C', 'D'
    val createdAt = datetime("created_at").default(currentKotlinDateTime()) // ✅ Corregido

    override val primaryKey = PrimaryKey(id)
}

object UserAnswersTable : Table("user_answers") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val questionId = integer("question_id").references(QuestionsTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val isCorrect = bool("is_correct")
    val answeredAt = datetime("answered_at").default(currentKotlinDateTime()) // ✅ Corregido

    override val primaryKey = PrimaryKey(id)
}

// ==========================================
// 📦 MODELOS DTO (SERIALIZABLES PARA JSON)
// ==========================================

@kotlinx.serialization.Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

@kotlinx.serialization.Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@kotlinx.serialization.Serializable
data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phone: String?,
    val totalPoints: Int,
    val currentLevel: Int,
    val currentExperience: Int,
    val token: String? = null
)

@kotlinx.serialization.Serializable
data class VerifyCodeRequest(
    val userId: Int,
    val code: String
)

@kotlinx.serialization.Serializable
data class QuestionResponse(
    val id: Int? = null,
    val category: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String
)