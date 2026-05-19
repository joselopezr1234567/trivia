package com.jldeveloper.trivia.data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Tabla de Usuarios para el Login y Sesión
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val email = varchar("email", length = 100).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 256) // Guardaremos la contraseña encriptada
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// Tabla de Puntajes para el Ranking Global
object Scores : Table("scores") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id) // Relación Foreign Key
    val points = integer("points")
    val levelReached = integer("level_reached")
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}