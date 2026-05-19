package cl.jlopezr.trivia.server.data.database

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class DatabaseTest {

    @Before
    fun setup() {
        // Inicializa una base de datos H2 en memoria limpia para cada test de forma aislada
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            // Crea las tablas de Exposed antes de ejecutar cada prueba
            SchemaUtils.create(Users, Scores)
        }
    }

    @Test
    fun `insert user and score successfully`() = transaction {
        // 1. Insertar un usuario de prueba en la tabla 'users'
        val generatedUserId = Users.insert {
            it[username] = "JLdeveloper"
            it[email] = "jl@developer.com"
            it[passwordHash] = "hashed_password_super_segura_123"
        } get Users.id

        // Validar que el ID autoincremental se generó correctamente
        assertNotNull(generatedUserId)

        // 2. Insertar un puntaje asociado a ese usuario en la tabla 'scores' (Foreign Key)
        Scores.insert {
            it[userId] = generatedUserId
            it[points] = 2500
            it[levelReached] = 8
        }

        // 3. Recuperar el registro y verificar que los datos coincidan
        val scoreRow = Scores.select { Scores.userId eq generatedUserId }.single()
        assertEquals(2500, scoreRow[Scores.points])
        assertEquals(8, scoreRow[Scores.levelReached])
    }

    @Test
    fun `insert duplicate username should fail`() = transaction {
        // 1. Insertar el primer usuario con un nombre de usuario específico
        Users.insert {
            it[username] = "clonUser"
            it[email] = "original@test.com"
            it[passwordHash] = "pass1"
        }

        // 2. Intentar insertar otro usuario con el mismo 'username' debe lanzar una excepción de SQL
        // debido a la restricción .uniqueIndex() que definimos en las tablas.
        assertFailsWith<ExposedSQLException> {
            Users.insert {
                it[username] = "clonUser" // Duplicado intencional para forzar el fallo
                it[email] = "otroEmail@test.com"
                it[passwordHash] = "pass2"
            }
        }
    }

    @Test
    fun `insert score with non existing userId should fail`() = transaction {
        // Intentar registrar un puntaje apuntando a un ID de usuario inexistente (ej: 999)
        // debe romper la restricción de clave foránea (.references)
        assertFailsWith<ExposedSQLException> {
            Scores.insert {
                it[userId] = 999
                it[points] = 500
                it[levelReached] = 2
            }
        }
    }
}