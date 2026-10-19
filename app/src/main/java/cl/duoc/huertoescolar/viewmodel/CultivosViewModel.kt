package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.CultivoRepository
import cl.duoc.huertoescolar.model.Cultivo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CultivosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CultivoRepository

    val cultivos: StateFlow<List<Cultivo>>

    init {
        val cultivoDao = AppDatabase.getInstance(application).cultivoDao()
        repository = CultivoRepository(cultivoDao)
        cultivos = repository.obtenerCultivos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}