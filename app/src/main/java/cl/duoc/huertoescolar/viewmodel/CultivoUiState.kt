package cl.duoc.huertoescolar.viewmodel

import cl.duoc.huertoescolar.model.EstadoCultivo

data class CultivoUiState(
    val nombre: String = "",
    val sectorId: Int? = null,
    val sectorTexto: String = "",
    val fechaSiembra: String = "",
    val fechaCosecha: String = "",
    val frecuenciaRiego: String = "",
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,

    val errorNombre: String? = null,
    val errorSector: String? = null,
    val errorSiembra: String? = null,
    val errorCosecha: String? = null,
    val errorRiego: String? = null,

    val mensaje: String = ""
)