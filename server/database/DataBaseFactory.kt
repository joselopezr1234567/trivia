package cl.jlopezr.trivia.server.data.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Clock.System

object DatabaseFactory {
    fun init() {
        // En Render configuraremos estas variables de entorno.
        // Si no existen, usará los valores por defecto locales.
        val jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/trivia_db"
        val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
        val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "password"

        val database = Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )

        // Crea las tablas de Exposed de forma segura al iniciar el servidor
        transaction(database) {
            SchemaUtils.create(Users, Scores)
        }
    }

    // Helper para ejecutar queries en un hilo separado (IO) de forma asíncrona
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}