package cl.duoc.huertoescolar.viewmodel

data class SectorUiState(
    val codigo: String = "",
    val nombre: String = "",
    val ubicacion: String = "",
    val enUso: Boolean = false,
    val confirma: Boolean = false,

    val errorCodigo: String? = null,
    val errorNombre: String? = null,
    val errorUbicacion: String? = null,
    val errorConfirma: String? = null,

    val mensaje: String = "",
    val fotoUri: String? = null
)