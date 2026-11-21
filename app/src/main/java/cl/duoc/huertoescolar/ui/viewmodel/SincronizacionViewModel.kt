package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.mensajeDeError
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface EstadoSincronizacion {
    data object Inactivo : EstadoSincronizacion
    data object Sincronizando : EstadoSincronizacion
    data object Completa : EstadoSincronizacion
    data class SinConexion(val mensaje: String) : EstadoSincronizacion
}

// Descarga del servidor los usuarios, sectores y cultivos y los deja en Room.
// Si no hay conexión, la app sigue mostrando los datos guardados en el dispositivo.
class SincronizacionViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sectorRepository: SectorRepository,
    private val cultivoRepository: CultivoRepository,
    private val sesion: SesionActual = SesionActual
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoSincronizacion>(EstadoSincronizacion.Inactivo)
    val estado: StateFlow<EstadoSincronizacion> = _estado.asStateFlow()

    fun sincronizar() {
        if (_estado.value == EstadoSincronizacion.Sincronizando) return
        viewModelScope.launch {
            _estado.value = EstadoSincronizacion.Sincronizando
            try {
                // Los usuarios van primero: los cultivos se asignan a ids de estudiante del servidor
                usuarioRepository.sincronizar()
                actualizarSesion()
                sectorRepository.sincronizar()
                cultivoRepository.sincronizar()
                _estado.value = EstadoSincronizacion.Completa
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoSincronizacion.SinConexion(mensajeDeError(e))
            }
        }
    }

    // Tras sincronizar, el id del usuario conectado puede haber cambiado al del servidor
    private suspend fun actualizarSesion() {
        val actual = sesion.usuario.value ?: return
        val refrescado = usuarioRepository.buscarPorCorreo(actual.correo) ?: return
        if (refrescado != actual) sesion.iniciar(refrescado)
    }
}
