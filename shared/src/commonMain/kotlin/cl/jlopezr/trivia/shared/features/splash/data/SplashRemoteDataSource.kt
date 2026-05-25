package cl.jlopezr.trivia.splash.data.datasource

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class SplashRemoteDataSource(private val httpClient: HttpClient) {

    private val baseUrl = "http://10.0.2.2:8080"

    /**
     * Envía el token al backend para validar si la sesión sigue activa.
     * Retorna verdadero si el backend responde que el token es válido (HTTP 200).
     */
    suspend fun validateToken(token: String): Boolean {
        return try {
            val response = httpClient.get("$baseUrl/auth/validate-token") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false // Si el servidor está caído o tira 401, asumimos sesión inválida
        }
    }
}
