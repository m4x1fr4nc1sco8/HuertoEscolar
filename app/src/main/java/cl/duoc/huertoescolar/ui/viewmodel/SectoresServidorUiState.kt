package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.data.remote.SectorDto

sealed interface SectoresServidorUiState {
    data object Cargando : SectoresServidorUiState

    data class Exito(
        val sectores: List<SectorDto>
    ) : SectoresServidorUiState

    data class Error(
        val mensaje: String
    ) : SectoresServidorUiState
}