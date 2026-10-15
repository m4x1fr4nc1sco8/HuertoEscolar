package cl.duoc.huertoescolar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.model.Rol
import cl.duoc.huertoescolar.util.Validaciones

class RegistroViewModel : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var apellido by mutableStateOf("")
        private set
    var correo by mutableStateOf("")
        private set
    var fechaNacimiento by mutableStateOf("")
        private set
    var clave by mutableStateOf("")
        private set
    var repetirClave by mutableStateOf("")
        private set
    var esAdministrador by mutableStateOf(false)
        private set
    var aceptaTerminos by mutableStateOf(false)
        private set

    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorApellido by mutableStateOf<String?>(null)
        private set
    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorFecha by mutableStateOf<String?>(null)
        private set
    var errorClave by mutableStateOf<String?>(null)
        private set
    var errorRepetir by mutableStateOf<String?>(null)
        private set
    var errorTerminos by mutableStateOf<String?>(null)
        private set

    var mensaje by mutableStateOf("")
        private set

    fun cambiarNombre(valor: String) { nombre = valor; errorNombre = null }
    fun cambiarApellido(valor: String) { apellido = valor; errorApellido = null }
    fun cambiarCorreo(valor: String) { correo = valor; errorCorreo = null }
    fun cambiarFecha(valor: String) { fechaNacimiento = valor; errorFecha = null }
    fun cambiarClave(valor: String) { clave = valor; errorClave = null }
    fun cambiarRepetir(valor: String) { repetirClave = valor; errorRepetir = null }
    fun cambiarEsAdministrador(valor: Boolean) { esAdministrador = valor; errorFecha = null }
    fun cambiarTerminos(valor: Boolean) { aceptaTerminos = valor; errorTerminos = null }

    fun registrar() {
        val rNombre = Validaciones.validarNombre(nombre, "El nombre")
        val rApellido = Validaciones.validarNombre(apellido, "El apellido")
        val rCorreo = Validaciones.validarCorreo(correo)
        val rFecha = Validaciones.validarFechaNacimiento(fechaNacimiento, esAdministrador)
        val rClave = Validaciones.validarClave(clave)
        val rRepetir = Validaciones.validarClavesIguales(clave, repetirClave)

        errorNombre = rNombre.mensaje
        errorApellido = rApellido.mensaje
        errorCorreo = rCorreo.mensaje
        errorFecha = rFecha.mensaje
        errorClave = rClave.mensaje
        errorRepetir = rRepetir.mensaje
        errorTerminos = if (aceptaTerminos) null else "Debes aceptar los términos"

        val formularioValido = rNombre.esValido && rApellido.esValido && rCorreo.esValido &&
                rFecha.esValido && rClave.esValido && rRepetir.esValido && aceptaTerminos

        if (formularioValido) {
            val perfil = if (esAdministrador) Rol.ADMINISTRADOR else Rol.ESTUDIANTE
            println("Usuario: $nombre $apellido | $correo | $perfil")
            mensaje = "Usuario registrado (prueba)"
        } else {
            mensaje = ""
        }
    }
}