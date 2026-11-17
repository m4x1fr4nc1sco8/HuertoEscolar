package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.domain.model.EstadoSector

data class SectorUiState(
    // null = creando un sector nuevo; con valor = editando ese sector
    val id: Int? = null,
    val codigo: String = "",
    val nombre: String = "",
    val ubicacion: String = "",
    val enUso: Boolean = false,
    // Solo se usa al editar (los 4 estados permitidos)
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val confirma: Boolean = false,

    val errorCodigo: String? = null,
    val errorNombre: String? = null,
    val errorUbicacion: String? = null,
    val errorConfirma: String? = null,

    val mensaje: String = "",
    val fotoUri: String? = null,
    // Se pone en true cuando una edición se guardó (la pantalla vuelve atrás)
    val guardado: Boolean = false
)