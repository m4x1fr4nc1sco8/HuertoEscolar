package cl.duoc.huertoescolar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.model.Sector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SectoresViewModel(
    repository: SectorRepository
) : ViewModel() {

    val sectores: StateFlow<List<Sector>> =
        repository
            .obtenerSectores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
