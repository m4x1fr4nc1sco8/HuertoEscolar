package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.UsuarioRepository
import cl.duoc.huertoescolar.model.Rol
import cl.duoc.huertoescolar.model.Usuario
import cl.duoc.huertoescolar.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = UsuarioRepository(database.usuarioDao())

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun cambiarApellido(valor: String) {
        _uiState.update { it.copy(apellido = valor, errorApellido = null) }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun cambiarFecha(valor: String) {
        _uiState.update { it.copy(fechaNacimiento = valor, errorFecha = null) }
    }

    fun cambiarClave(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null) }
    }

    fun cambiarRepetir(valor: String) {
        _uiState.update { it.copy(repetirClave = valor, errorRepetir = null) }
    }

    fun cambiarEsAdministrador(valor: Boolean) {
        _uiState.update { it.copy(esAdministrador = valor, errorFecha = null) }
    }

    fun cambiarTerminos(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    fun registrar() {
        val estado = _uiState.value

        val rNombre = Validaciones.validarNombre(estado.nombre, "El nombre")
        val rApellido = Validaciones.validarNombre(estado.apellido, "El apellido")
        val rCorreo = Validaciones.validarCorreo(estado.correo)
        val rFecha = Validaciones.validarFechaNacimiento(
            estado.fechaNacimiento, estado.esAdministrador
        )
        val rClave = Validaciones.validarClave(estado.clave)
        val rRepetir = Validaciones.validarClavesIguales(estado.clave, estado.repetirClave)

        _uiState.update {
            it.copy(
                errorNombre = rNombre.mensaje,
                errorApellido = rApellido.mensaje,
                errorCorreo = rCorreo.mensaje,
                errorFecha = rFecha.mensaje,
                errorClave = rClave.mensaje,
                errorRepetir = rRepetir.mensaje,
                errorTerminos = if (estado.aceptaTerminos) null else "Debes aceptar los términos",
                mensaje = ""
            )
        }

        val formularioValido = rNombre.esValido && rApellido.esValido &&
                rCorreo.esValido && rFecha.esValido && rClave.esValido &&
                rRepetir.esValido && estado.aceptaTerminos

        if (formularioValido) {
            val tipo = if (estado.esAdministrador) Rol.ADMINISTRADOR else Rol.ESTUDIANTE
            val usuario = Usuario(
                nombre = estado.nombre.trim(),
                apellido = estado.apellido.trim(),
                correo = estado.correo.trim(),
                fechaNacimiento = estado.fechaNacimiento.trim(),
                clave = estado.clave,
                tipoUsuario = tipo.name
            )
            viewModelScope.launch {
                repository.guardar(usuario)
                _uiState.value = RegistroUiState(mensaje = "Usuario registrado correctamente")
            }
        }
    }
}