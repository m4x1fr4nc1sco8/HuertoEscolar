package cl.duoc.huertoescolar.ui.viewmodel

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)