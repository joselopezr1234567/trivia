package cl.jlopezr.trivia.server

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcUrl = "jdbc:postgresql://localhost:5432/trivia_db"
        val user = "macbook"
        val password = ""

        val database = Database.connect(jdbcUrl, driverClassName, user, password)

        // Esto le dice a Exposed que registre tus tablas en su mapa de ejecución
        transaction(database) {
            SchemaUtils.create(UsersTable, UserPointsTable, UserLevelsTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}