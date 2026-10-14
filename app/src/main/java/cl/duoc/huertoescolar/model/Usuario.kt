package cl.duoc.huertoescolar.model

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: Rol,
    val grupo: String
)