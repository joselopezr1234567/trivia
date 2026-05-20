package cl.jlopezr.trivia.server

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.* // ✅ IMPORT CRUCIAL PARA EL CLIENTE
import io.ktor.serialization.kotlinx.json.* // ✅ IMPORT CRUCIAL PARA EL CLIENTE
import kotlinx.serialization.json.*
import cl.jlopezr.trivia.server.QuestionResponse // 👈 Asegúrate que apunte a tu DTO
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString

class OpenAiService {

    private val apiKey = System.getenv("OPENAI_API_KEY") ?: ""
    private val apiUrl = "https://api.openai.com/v1/chat/completions"

    // 🛠️ SE EQUIPA EL CLIENTE CON CONTENT NEGOTIATION PARA PODER ENVIAR EL JSON
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun generateQuestionFromAi(category: String): QuestionResponse? {
        val systemPrompt = """
            Eres un motor experto de juegos de trivia. Tu única tarea es generar una pregunta de opción múltiple sobre la categoría solicitada.
            Debes responder exclusivamente en formato JSON estructurado, sin rodeos, sin bloques de código ```json ... ``` ni marcas de texto.
            El JSON debe cumplir con estos campos exactos:
            {
              "category": "$category",
              "questionText": "Texto de la pregunta",
              "optionA": "Opción A",
              "optionB": "Opción B",
              "optionC": "Opción C",
              "optionD": "Opción D",
              "correctOption": "A"
            }
            La propiedad 'correctOption' debe ser estrictamente una sola letra mayúscula: 'A', 'B', 'C' o 'D'.
        """.trimIndent()

        try {
            val response: HttpResponse = client.post(apiUrl) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("model", "gpt-4o-mini")
                    put("messages", buildJsonArray {
                        addJsonObject {
                            put("role", "system")
                            put("content", systemPrompt)
                        }
                        addJsonObject {
                            put("role", "user")
                            put("content", "Genera una pregunta de dificultad media para la categoría: $category")
                        }
                    })
                    put("temperature", 0.7)
                })
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val jsonElement = Json.parseToJsonElement(responseBody)

                val rawContent = jsonElement.jsonObject["choices"]?.jsonArray?.get(0)
                    ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content ?: return null

                return Json.decodeFromString<QuestionResponse>(rawContent)
            } else {
                println("❌ Error de OpenAI: ${response.status} - ${response.bodyAsText()}")
                return null
            }
        } catch (e: Exception) {
            println("⚠️ Excepción al conectar con OpenAI: ${e.localizedMessage}")
            return null
        }
    }
}