package cl.jlopezr.trivia.shared.core.data

/**
 * Objeto Singleton para persistir los datos durante la sesión.
 * En el futuro, aquí podrías usar DataStore para que no se borre al cerrar la app.
 */
object ProgressStorage {
    var totalScore: Int = 0
    var currentLevel: Int = 1
}