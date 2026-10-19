package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.CultivoRepository
import cl.duoc.huertoescolar.data.SesionActual
import cl.duoc.huertoescolar.model.Cultivo
import cl.duoc.huertoescolar.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CultivosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        CultivoRepository(AppDatabase.getInstance(application).cultivoDao())

    // Estudiante: solo sus cultivos asignados. Admin: todos.
    @OptIn(ExperimentalCoroutinesApi::class)
    val cultivos: StateFlow<List<Cultivo>> =
        SesionActual.usuario
            .flatMapLatest { usuario ->
                if (usuario != null && usuario.tipoUsuario == Rol.ESTUDIANTE.name) {
                    repository.obtenerPorEstudiante(usuario.id)
                } else {
                    repository.obtenerCultivos()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun marcarComoRealizada(cultivoId: Int, fotoUri: String) {
        val usuario = SesionActual.usuario.value ?: return
        viewModelScope.launch {
            val ok = repository.marcarComoRealizada(cultivoId, usuario.id, fotoUri)
            _mensaje.value = if (ok) {
                "Tarea enviada. Queda pendiente de validación."
            } else {
                "No se pudo enviar la tarea."
            }
        }
    }

    private fun esAdministrador(): Boolean =
        SesionActual.usuario.value?.tipoUsuario == Rol.ADMINISTRADOR.name

    // Admin: valida la tarea enviada por el estudiante
    fun validar(cultivoId: Int) {
        if (!esAdministrador()) return
        viewModelScope.launch {
            val ok = repository.validar(cultivoId)
            _mensaje.value = if (ok) "Tarea validada." else "No se pudo validar la tarea."
        }
    }

    private val _errorMotivo = MutableStateFlow<String?>(null)
    val errorMotivo: StateFlow<String?> = _errorMotivo.asStateFlow()

    fun limpiarErrorMotivo() {
        _errorMotivo.value = null
    }

    // Admin: rechaza con motivo; la tarea vuelve a pendiente.
    // Devuelve true si el motivo es válido y se envió el rechazo.
    fun rechazar(cultivoId: Int, motivo: String): Boolean {
        if (!esAdministrador()) return false
        if (motivo.isBlank()) {
            _errorMotivo.value = "Escribe el motivo del rechazo"
            return false
        }
        _errorMotivo.value = null
        viewModelScope.launch {
            val ok = repository.rechazar(cultivoId, motivo)
            _mensaje.value = if (ok) {
                "Tarea rechazada. El estudiante podrá reenviarla."
            } else {
                "No se pudo rechazar la tarea."
            }
        }
        return true
    }

    fun informar(texto: String) {
        _mensaje.value = texto
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}