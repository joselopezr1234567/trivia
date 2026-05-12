package cl.jlopezr.trivia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform