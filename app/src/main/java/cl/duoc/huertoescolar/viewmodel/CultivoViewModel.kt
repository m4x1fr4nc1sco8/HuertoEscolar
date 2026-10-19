package cl.duoc.huertoescolar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.CultivoRepository
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.model.Cultivo
import cl.duoc.huertoescolar.model.EstadoCultivo
import cl.duoc.huertoescolar.model.Sector
import cl.duoc.huertoescolar.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CultivoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = CultivoRepository(database.cultivoDao())
    private val sectorRepository = SectorRepository(database.sectorDao())

    val sectores: StateFlow<List<Sector>> =
        sectorRepository
            .obtenerSectores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _uiState = MutableStateFlow(CultivoUiState())
    val uiState: StateFlow<CultivoUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun seleccionarSector(sector: Sector) {
        _uiState.update {
            it.copy(
                sectorId = sector.id,
                sectorTexto = "${sector.codigo} · ${sector.nombre}",
                errorSector = null
            )
        }
    }

    fun cambiarSiembra(valor: String) {
        _uiState.update { it.copy(fechaSiembra = valor, errorSiembra = null, errorCosecha = null) }
    }

    fun cambiarCosecha(valor: String) {
        _uiState.update { it.copy(fechaCosecha = valor, errorCosecha = null) }
    }

    fun cambiarRiego(valor: String) {
        _uiState.update { it.copy(frecuenciaRiego = valor, errorRiego = null) }
    }

    fun cambiarEstado(valor: EstadoCultivo) {
        _uiState.update { it.copy(estado = valor) }
    }

    fun limpiar() {
        _uiState.value = CultivoUiState()
    }

    fun guardar() {
        val estado = _uiState.value

        val rNombre = Validaciones.validarNombre(estado.nombre, "El nombre del cultivo")
        val rSiembra = Validaciones.validarFecha(estado.fechaSiembra, "La fecha de siembra")
        val rCosecha = Validaciones.validarCosechaPosterior(
            estado.fechaSiembra, estado.fechaCosecha
        )
        val rRiego = Validaciones.validarFrecuenciaRiego(estado.frecuenciaRiego)
        val sectorId = estado.sectorId

        _uiState.update {
            it.copy(
                errorNombre = rNombre.mensaje,
                errorSector = if (sectorId == null) "Selecciona un sector" else null,
                errorSiembra = rSiembra.mensaje,
                errorCosecha = rCosecha.mensaje,
                errorRiego = rRiego.mensaje,
                mensaje = ""
            )
        }

        val formularioValido = rNombre.esValido && rSiembra.esValido &&
                rCosecha.esValido && rRiego.esValido && sectorId != null

        if (formularioValido && sectorId != null) {
            val cultivo = Cultivo(
                nombre = estado.nombre.trim(),
                sectorId = sectorId,
                fechaSiembra = estado.fechaSiembra.trim(),
                fechaCosecha = estado.fechaCosecha.trim(),
                frecuenciaRiegoDias = estado.frecuenciaRiego.toInt(),
                estado = estado.estado.name
            )
            viewModelScope.launch {
                repository.guardar(cultivo)
                _uiState.value = CultivoUiState(mensaje = "Cultivo guardado correctamente")
            }
        }
    }
}