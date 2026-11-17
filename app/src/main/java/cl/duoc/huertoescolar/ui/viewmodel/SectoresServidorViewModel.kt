package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.SectoresRemoteRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SectoresServidorViewModel(
    private val repository: SectoresRemoteRepository = SectoresRemoteRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SectoresServidorUiState>(SectoresServidorUiState.Cargando)
    val uiState: StateFlow<SectoresServidorUiState> = _uiState.asStateFlow()

    init {
        cargarSectores()
    }

    fun cargarSectores() {
        viewModelScope.launch {
            _uiState.value = SectoresServidorUiState.Cargando

            try {
                val sectores = repository.obtenerSectores()
                _uiState.value = SectoresServidorUiState.Exito(sectores)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SectoresServidorUiState.Error(
                    "No se pudo conectar con el servidor. " +
                            "Comprueba la conexión e inténtalo nuevamente."
                )
            }
        }
    }
}