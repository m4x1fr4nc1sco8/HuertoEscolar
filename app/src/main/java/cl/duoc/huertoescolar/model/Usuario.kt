package cl.duoc.huertoescolar.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val fechaNacimiento: String,
    val clave: String,
    val tipoUsuario: String
)