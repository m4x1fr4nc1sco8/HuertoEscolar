package cl.duoc.huertoescolar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.model.EstadoSector
import cl.duoc.huertoescolar.util.Validaciones

class SectorViewModel : ViewModel() {

    // Variables
    var codigo by mutableStateOf("")
        private set
    var nombre by mutableStateOf("")
        private set
    var ubicacion by mutableStateOf("")
        private set
    var enUso by mutableStateOf(false)
        private set
    var confirma by mutableStateOf(false)
        private set

    // Estados de error
    var errorCodigo by mutableStateOf<String?>(null)
        private set
    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorUbicacion by mutableStateOf<String?>(null)
        private set
    var errorConfirma by mutableStateOf<String?>(null)
        private set

    var mensaje by mutableStateOf("")
        private set

    fun cambiarCodigo(valor: String) { codigo = valor; errorCodigo = null }
    fun cambiarNombre(valor: String) { nombre = valor; errorNombre = null }
    fun cambiarUbicacion(valor: String) { ubicacion = valor; errorUbicacion = null }
    fun cambiarEnUso(valor: Boolean) { enUso = valor }
    fun cambiarConfirma(valor: Boolean) { confirma = valor; errorConfirma = null }

    fun guardar() {
        val rCodigo = Validaciones.validarObligatorio(codigo, "El código")
        val rNombre = Validaciones.validarNombre(nombre, "El nombre del sector")
        val rUbicacion = Validaciones.validarObligatorio(ubicacion, "La ubicación")
        errorCodigo = rCodigo.mensaje
        errorNombre = rNombre.mensaje
        errorUbicacion = rUbicacion.mensaje
        errorConfirma = if (confirma) null else "Debes confirmar los datos"

        val formularioValido = rCodigo.esValido && rNombre.esValido &&
                rUbicacion.esValido && confirma

        if (formularioValido) {
            val estado = if (enUso) EstadoSector.EN_USO else EstadoSector.DISPONIBLE
            println("Sector: $codigo | $nombre | $ubicacion | $estado")
            mensaje = "Sector guardado (prueba)"
        } else {
            mensaje = ""
        }
    }

    fun limpiar() {
        codigo = ""
        nombre = ""
        ubicacion = ""
        enUso = false
        confirma = false
        errorCodigo = null
        errorNombre = null
        errorUbicacion = null
        errorConfirma = null
        mensaje = ""
    }
}