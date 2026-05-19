package cl.jlopezr.trivia.server

interface SmsService {
    suspend fun sendVerificationSms(phone: String, code: String)
}

class FakeSmsServiceImplementation : SmsService {
    override suspend fun sendVerificationSms(phone: String, code: String) {
        // 🔥 MODO HARDCORE: Simulamos el envío imprimiendo un bloque llamativo en la consola de tu Mac
        println("\n========================================================")
        println("📱 [SMS SIMULADO] Enviando mensaje al número: $phone")
        println("💬 Mensaje: Tu código de verificación de Trivia es: $code")
        println("========================================================\n")
    }
}
