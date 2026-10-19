package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.model.EstadoSector
import cl.duoc.huertoescolar.model.Sector
import cl.duoc.huertoescolar.util.Validaciones
import cl.duoc.huertoescolar.util.guardarFotoPermanente
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SectorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SectorRepository(database.sectorDao())

    private val _uiState = MutableStateFlow(SectorUiState())
    val uiState: StateFlow<SectorUiState> = _uiState.asStateFlow()

    fun cambiarCodigo(valor: String) {
        _uiState.update { it.copy(codigo = valor, errorCodigo = null) }
    }

    fun cambiarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun cambiarUbicacion(valor: String) {
        _uiState.update { it.copy(ubicacion = valor, errorUbicacion = null) }
    }

    fun cambiarEnUso(valor: Boolean) {
        _uiState.update { it.copy(enUso = valor) }
    }

    fun cambiarConfirma(valor: Boolean) {
        _uiState.update { it.copy(confirma = valor, errorConfirma = null) }
    }

    fun cambiarFoto(uri: String?) {
        _uiState.update { it.copy(fotoUri = uri) }
    }

    fun quitarFoto() {
        _uiState.update { it.copy(fotoUri = null) }
    }

    fun limpiar() {
        _uiState.value = SectorUiState()
    }

    fun guardar() {
        val estado = _uiState.value

        val rCodigo = Validaciones.validarObligatorio(estado.codigo, "El código")
        val rNombre = Validaciones.validarNombre(estado.nombre, "El nombre del sector")
        val rUbicacion = Validaciones.validarObligatorio(estado.ubicacion, "La ubicación")

        _uiState.update {
            it.copy(
                errorCodigo = rCodigo.mensaje,
                errorNombre = rNombre.mensaje,
                errorUbicacion = rUbicacion.mensaje,
                errorConfirma = if (estado.confirma) null else "Debes confirmar los datos",
                mensaje = ""
            )
        }

        val formularioValido = rCodigo.esValido && rNombre.esValido &&
                rUbicacion.esValido && estado.confirma

        if (formularioValido) {
            val estadoSector =
                if (estado.enUso) EstadoSector.EN_USO else EstadoSector.DISPONIBLE
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            viewModelScope.launch {
                if (repository.existeCodigo(estado.codigo.trim())) {
                    _uiState.update {
                        it.copy(errorCodigo = "Ya existe un sector con ese código")
                    }
                    return@launch
                }
                val rutaFoto = estado.fotoUri?.let { temporal ->
                    withContext(Dispatchers.IO) {
                        guardarFotoPermanente(
                            getApplication<Application>(),
                            Uri.parse(temporal)
                        )
                    }
                }

                val sector = Sector(
                    codigo = estado.codigo.trim(),
                    nombre = estado.nombre.trim(),
                    ubicacion = estado.ubicacion.trim(),
                    estado = estadoSector.name,
                    fechaCreacion = fecha,
                    fotoUri = rutaFoto
                )
                repository.guardar(sector)
                _uiState.value = SectorUiState(mensaje = "Sector guardado correctamente")
            }
        }
    }
}