package cl.duoc.huertoescolar.viewmodel

data class RegistroUiState(
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val clave: String = "",
    val repetirClave: String = "",
    val esAdministrador: Boolean = false,
    val aceptaTerminos: Boolean = false,

    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorCorreo: String? = null,
    val errorFecha: String? = null,
    val errorClave: String? = null,
    val errorRepetir: String? = null,
    val errorTerminos: String? = null,

    val mensaje: String = ""
)