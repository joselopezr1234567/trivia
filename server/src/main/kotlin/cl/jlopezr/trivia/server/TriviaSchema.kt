package cl.jlopezr.trivia.server

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Mapeo actualizado de la Tabla de Usuarios
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = text("password") // Aquí Ktor procesará la contraseña encriptada
    val phone = varchar("phone", 20).uniqueIndex().nullable() // .nullable() por si decides dejarlo opcional
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val verificationCode = varchar("verification_code", 6).default("000000")

    override val primaryKey = PrimaryKey(id)
}

// Mapeo de la Tabla de Puntos
object UserPointsTable : Table("user_points") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()
    val totalPoints = integer("total_points").default(0)
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// Mapeo de la Tabla de Niveles
object UserLevelsTable : Table("user_levels") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE).uniqueIndex()
    val currentLevel = integer("current_level").default(1)
    val currentExperience = integer("current_experience").default(0)
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
// Lo que nos enviará la app móvil al registrarse
@kotlinx.serialization.Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

// Lo que nos enviará la app móvil al iniciar sesión
@kotlinx.serialization.Serializable
data class UserLoginRequest(
    val email: String,  // El usuario podrá entrar con su email
    val password: String
)

// Lo que el servidor le responderá a la app (¡NUNCA devolvemos la contraseña!)
@kotlinx.serialization.Serializable
data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phone: String?,
    val totalPoints: Int,
    val currentLevel: Int,
    val currentExperience: Int,
    val token: String? = null // 👈 Agregamos el token aquí (opcional por defecto)
)

@kotlinx.serialization.Serializable
data class VerifyCodeRequest(
    val userId: Int,
    val code: String
)