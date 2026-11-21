package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.mensajeDeError
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.domain.model.Sector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SectoresViewModel(
    private val repository: SectorRepository
) : ViewModel() {

    val sectores: StateFlow<List<Sector>> =
        repository
            .obtenerSectores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Elimina el sector en el servidor; si todavía tiene cultivos, el servidor lo rechaza
    fun eliminar(id: Int) {
        viewModelScope.launch {
            try {
                repository.eliminar(id)
                _mensaje.value = "Sector eliminado."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = mensajeDeError(e)
            }
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
