package cl.duoc.huertoescolar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.util.Validaciones

class LoginViewModel : ViewModel() {

    // Variables
    var correo by mutableStateOf("")
        private set
    var clave by mutableStateOf("")
        private set

    // Estados de error
    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorClave by mutableStateOf<String?>(null)
        private set

    fun cambiarCorreo(valor: String) {
        correo = valor
        errorCorreo = null
    }

    fun cambiarClave(valor: String) {
        clave = valor
        errorClave = null
    }

    // Acción del formulario: devuelve true si los datos son válidos
    fun ingresar(): Boolean {
        val rCorreo = Validaciones.validarCorreo(correo)
        val rClave = Validaciones.validarClave(clave)
        errorCorreo = rCorreo.mensaje
        errorClave = rClave.mensaje
        return rCorreo.esValido && rClave.esValido
    }
}