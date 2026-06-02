package cl.jlopezr.network

import io.ktor.client.*
import io.ktor.client.call.* // IMPORTANTE para .body()
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(val email: String, val password: String)

// AÑADE ESTA CLASE para que coincida con el JSON del servidor
@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String
)

val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
}

suspend fun login(user: String, pass: String): Boolean {
    return try {
        val response = client.post("http://10.0.2.2:8080/auth/login") { // Asegúrate que la ruta sea /auth/login
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(user, pass))
        }

        // En lugar de solo mirar el status, leemos el JSON de respuesta
        if (response.status == HttpStatusCode.OK) {
            val loginResponse = response.body<LoginResponse>()
            loginResponse.success // Retorna true si el servidor envió success: true
        } else {
            false
        }
    } catch (e: Exception) {
        println("❌ ERROR DE RED: ${e.message}")
        e.printStackTrace()
        false
    }
}