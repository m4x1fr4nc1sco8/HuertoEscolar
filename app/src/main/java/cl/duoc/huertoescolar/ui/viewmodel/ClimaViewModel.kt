package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.ClimaRepository
import cl.duoc.huertoescolar.domain.model.Clima
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface ClimaUiState {
    data object Cargando : ClimaUiState
    data class Exito(val clima: Clima) : ClimaUiState
    data class Error(val mensaje: String) : ClimaUiState
}

class ClimaViewModel(
    private val repository: ClimaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<ClimaUiState>(ClimaUiState.Cargando)
    val estado: StateFlow<ClimaUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = ClimaUiState.Cargando
            try {
                _estado.value = ClimaUiState.Exito(repository.obtenerClima())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ClimaUiState.Error("No se pudo obtener el clima. Comprueba la conexión.")
            }
        }
    }
}
