package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.SesionActual
import cl.duoc.huertoescolar.data.UsuarioRepository
import cl.duoc.huertoescolar.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = UsuarioRepository(database.usuarioDao())

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.sembrarCuentasDePrueba()
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun cambiarClave(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun ingresar(onExitoso: () -> Unit) {
        val estado = _uiState.value

        val rCorreo = Validaciones.validarCorreo(estado.correo)
        val rClave = Validaciones.validarClave(estado.clave)

        _uiState.update {
            it.copy(
                errorCorreo = rCorreo.mensaje,
                errorClave = rClave.mensaje,
                errorGeneral = null
            )
        }

        if (!rCorreo.esValido || !rClave.esValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }

            val usuario = repository.buscarPorCorreo(estado.correo.trim())

            if (usuario != null && usuario.clave == estado.clave) {
                SesionActual.iniciar(usuario)
                _uiState.value = LoginUiState()
                onExitoso()
            } else {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "Correo o contraseña incorrectos"
                    )
                }
            }
        }
    }
}