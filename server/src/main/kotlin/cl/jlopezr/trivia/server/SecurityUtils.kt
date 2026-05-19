package cl.jlopezr.trivia.server

import java.security.MessageDigest
import java.util.Base64

object SecurityUtils {
    // ⚠️ IMPORTANTE: Cambia este Salt por una frase secreta aleatoria y larga.
    // Evita que usen tablas de arcoíris (Rainbow Tables) para crackear las contraseñas.
    private const val SECRET_SALT = "JLdeveloper_Trivia_Super_Secret_Salt_2026_#"

    fun hashPassword(password: String): String {
        val saltedPassword = password + SECRET_SALT
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))

        // Lo transformamos a Base64 para guardarlo como un String limpio en PostgreSQL
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun verifyPassword(password: String, hashed: String): Boolean {
        // Encripta la contraseña que ingresa el usuario en el Login y la compara con la de la BD
        return hashPassword(password) == hashed
    }
}