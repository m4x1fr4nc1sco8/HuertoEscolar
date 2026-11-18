package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.alertas.GeneradorAlertas
import cl.duoc.huertoescolar.domain.model.Alerta
import cl.duoc.huertoescolar.domain.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlertasViewModel(
    repository: CultivoRepository,
    sesion: SesionActual = SesionActual
) : ViewModel() {

    // Estudiante: alertas de sus cultivos asignados. Admin: alertas de todos los cultivos.
    @OptIn(ExperimentalCoroutinesApi::class)
    val alertas: StateFlow<List<Alerta>> =
        sesion.usuario
            .flatMapLatest { usuario ->
                if (usuario != null && usuario.tipoUsuario == Rol.ESTUDIANTE.name) {
                    repository.obtenerPorEstudiante(usuario.id)
                } else {
                    repository.obtenerCultivos()
                }
            }
            .map { cultivos -> GeneradorAlertas.generar(cultivos) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
