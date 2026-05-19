package cl.jlopezr.trivia.server

interface UserRepository {
    // Devuelve el perfil completo si el registro es exitoso, o null si el usuario/email ya existe
    suspend fun registerUser(request: UserRegisterRequest): UserProfileResponse?

    // Devuelve el perfil si las credenciales son correctas, o null si falla el login
    suspend fun loginUser(request: UserLoginRequest): UserProfileResponse?

    // Obtiene el estado actual del usuario por su ID
    suspend fun getUserProfile(userId: Int): UserProfileResponse?

    suspend fun verifySmsCode(userId: Int, code: String): Boolean
}