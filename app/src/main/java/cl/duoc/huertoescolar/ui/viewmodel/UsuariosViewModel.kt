package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UsuariosViewModel(
    repository: UsuarioRepository
) : ViewModel() {

    val usuarios: StateFlow<List<Usuario>> =
        repository
            .obtenerUsuarios()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}