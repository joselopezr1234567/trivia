package cl.jlopezr.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

// Debes tener esta misma data class en el backend,
// o mejor aún, moverla al módulo :shared para compartirla entre ambos
@Serializable
data class LoginRequest(val username: String, val password: String)

val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun login(user: String, pass: String): Boolean {
    return try {
        // En Android, localhost es 10.0.2.2. En iOS es localhost.
        val response = client.post("http://10.0.2.2:8080/login") {
            header("Content-Type", "application/json")
            setBody(LoginRequest(user, pass))
        }
        response.status.value == 200
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}