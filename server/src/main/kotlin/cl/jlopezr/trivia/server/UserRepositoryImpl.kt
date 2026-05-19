package cl.jlopezr.trivia.server

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepositoryImpl : UserRepository {

    override suspend fun registerUser(request: UserRegisterRequest): UserProfileResponse? = DatabaseFactory.dbQuery {
        // Verificar si el username o email ya existen para evitar colisiones
        val userExists = UsersTable.select {
            (UsersTable.username eq request.username) or (UsersTable.email eq request.email)
        }.count() > 0

        if (userExists) return@dbQuery null

        // 1. Encriptar contraseña antes de guardar
        val hashedPassword = SecurityUtils.hashPassword(request.password)

        // 2. Insertar en la tabla principal 'users'
        val generatedId = UsersTable.insert {
            it[username] = request.username
            it[email] = request.email
            it[password] = hashedPassword
            it[phone] = request.phone
        } get UsersTable.id

        // 3. Inicializar su tabla de puntos en 0
        UserPointsTable.insert {
            it[userId] = generatedId
            it[totalPoints] = 0
        }

        // 4. Inicializar su tabla de niveles en 1
        UserLevelsTable.insert {
            it[userId] = generatedId
            it[currentLevel] = 1
            it[currentExperience] = 0
        }

        UserProfileResponse(
            id = generatedId,
            username = request.username,
            email = request.email,
            phone = request.phone,
            totalPoints = 0,
            currentLevel = 1,
            currentExperience = 0
        )
    }

    override suspend fun loginUser(request: UserLoginRequest): UserProfileResponse? = DatabaseFactory.dbQuery {
        // Buscar al usuario por su email
        val userRow = UsersTable.select { UsersTable.email eq request.email }.singleOrNull() ?: return@dbQuery null

        val dbHashedPassword = userRow[UsersTable.password]

        // Verificar si la contraseña ingresada coincide con el hash de la BD
        if (!SecurityUtils.verifyPassword(request.password, dbHashedPassword)) {
            return@dbQuery null
        }

        // Si la contraseña es correcta, recuperamos su perfil completo cruzando las tablas
        getUserProfile(userRow[UsersTable.id])
    }

    override suspend fun getUserProfile(userId: Int): UserProfileResponse? = DatabaseFactory.dbQuery {
        val userRow = UsersTable.select { UsersTable.id eq userId }.singleOrNull() ?: return@dbQuery null

        // Buscamos sus puntos (si por alguna razón no tiene, por defecto es 0)
        val pointsRow = UserPointsTable.select { UserPointsTable.userId eq userId }.singleOrNull()
        val totalPoints = pointsRow?.get(UserPointsTable.totalPoints) ?: 0

        // Buscamos su nivel (por defecto nivel 1)
        val levelsRow = UserLevelsTable.select { UserLevelsTable.userId eq userId }.singleOrNull()
        val currentLevel = levelsRow?.get(UserLevelsTable.currentLevel) ?: 1
        val currentExperience = levelsRow?.get(UserLevelsTable.currentExperience) ?: 0

        UserProfileResponse(
            id = userId,
            username = userRow[UsersTable.username],
            email = userRow[UsersTable.email],
            phone = userRow[UsersTable.phone],
            totalPoints = totalPoints,
            currentLevel = currentLevel,
            currentExperience = currentExperience
        )
    }
}