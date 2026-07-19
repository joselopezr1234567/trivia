package cl.jlopezr.trivia

interface Platform {
    val name: String
    val language: String // Código de idioma (ej: "es", "en")
}

expect fun getPlatform(): Platform