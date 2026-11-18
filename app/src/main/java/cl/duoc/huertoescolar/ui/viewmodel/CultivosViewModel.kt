package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.validation.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CultivosViewModel(
    private val repository: CultivoRepository,
    private val sesion: SesionActual = SesionActual
) : ViewModel() {

    // Estudiante: solo sus cultivos asignados. Admin: todos.
    @OptIn(ExperimentalCoroutinesApi::class)
    val cultivos: StateFlow<List<Cultivo>> =
        sesion.usuario
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

    private val _errorComentario = MutableStateFlow<String?>(null)
    val errorComentario: StateFlow<String?> = _errorComentario.asStateFlow()

    fun limpiarErrorComentario() {
        _errorComentario.value = null
    }

    // Estudiante: revisa que el comentario no esté vacío antes de abrir la cámara.
    // Devuelve true si es válido.
    fun validarComentario(comentario: String): Boolean {
        val resultado = Validaciones.validarObligatorio(comentario, "El comentario")
        _errorComentario.value = resultado.mensaje
        return resultado.esValido
    }

    fun marcarComoRealizada(cultivoId: Int, fotoUri: String, comentario: String) {
        val usuario = sesion.usuario.value ?: return
        if (comentario.isBlank()) {
            _mensaje.value = "Escribe un comentario para enviar la tarea."
            return
        }
        viewModelScope.launch {
            val ok = repository.marcarComoRealizada(cultivoId, usuario.id, fotoUri, comentario)
            _mensaje.value = if (ok) {
                "Tarea enviada. Queda pendiente de validación."
            } else {
                "No se pudo enviar la tarea."
            }
        }
    }

    private fun esAdministrador(): Boolean =
        sesion.usuario.value?.tipoUsuario == Rol.ADMINISTRADOR.name

    // Admin: cambia el estado de desarrollo del cultivo (la lista se actualiza sola)
    fun cambiarEstado(cultivoId: Int, estado: EstadoCultivo) {
        if (!esAdministrador()) return
        viewModelScope.launch {
            val ok = repository.cambiarEstado(cultivoId, estado)
            _mensaje.value = if (ok) {
                "Estado actualizado a: ${estado.name.lowercase().replace('_', ' ')}"
            } else {
                "No se pudo actualizar el estado."
            }
        }
    }

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