package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.model.Sector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SectoresViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SectorRepository(database.sectorDao())

    val sectores: StateFlow<List<Sector>> =
        repository
            .obtenerSectores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}