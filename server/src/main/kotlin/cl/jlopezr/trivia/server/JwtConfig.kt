package cl.jlopezr.trivia.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "JLdeveloper_Trivia_JWT_Super_Secret_Key_2026_$" // Llave para firmar el token
    private const val ISSUER = "cl.jlopezr.trivia"
    private const val AUDIENCE = "trivia-app-client"

    // El token expirará en 30 días
    private const val VALIDITY_MS = 30L * 24 * 60 * 60 * 1000

    fun generateToken(userId: Int): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId) // Guardamos el ID del usuario dentro del token
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }
}