package cl.jlopezr.trivia.login.data.datasource

import LoginRequest
import LoginResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class LoginRemoteDataSource(private val httpClient: HttpClient) {

    private val baseUrl = "http://10.0.2.2:8080"

    suspend fun login(email: String, password: String): LoginResponse {
        val response = httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password = password))
        }

        // Si el servidor responde 200 OK
        if (response.status == HttpStatusCode.OK) {
            return response.body<LoginResponse>()
        } else {
            // Si hay un error (como el 400 que te salía), leemos el error como texto
            val errorBody = response.bodyAsText()
            println("❌ Error del servidor (${response.status}): $errorBody")

            // Retornamos un objeto con el mensaje de error para que la UI sepa qué pasó
            return LoginResponse(message = "Error: $errorBody")
        }
    }
}