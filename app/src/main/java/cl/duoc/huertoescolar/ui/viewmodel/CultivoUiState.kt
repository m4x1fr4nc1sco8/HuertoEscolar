package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.domain.model.EstadoCultivo

data class CultivoUiState(
    val nombre: String = "",
    val sectorId: Int? = null,
    val sectorTexto: String = "",
    val estudianteId: Int? = null,
    val estudianteTexto: String = "",
    val fechaSiembra: String = "",
    val fechaCosecha: String = "",
    val fechaLimite: String = "",
    val frecuenciaRiego: String = "",
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,

    val errorNombre: String? = null,
    val errorSector: String? = null,
    val errorEstudiante: String? = null,
    val errorSiembra: String? = null,
    val errorCosecha: String? = null,
    val errorLimite: String? = null,
    val errorRiego: String? = null,

    val mensaje: String = ""
)