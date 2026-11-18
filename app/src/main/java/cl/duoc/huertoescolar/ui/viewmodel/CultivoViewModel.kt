package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.model.Usuario
import cl.duoc.huertoescolar.domain.validation.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CultivoViewModel(
    private val repository: CultivoRepository,
    sectorRepository: SectorRepository,
    usuarioRepository: UsuarioRepository
) : ViewModel() {

    val sectores: StateFlow<List<Sector>> =
        sectorRepository
            .obtenerSectores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val estudiantes: StateFlow<List<Usuario>> =
        usuarioRepository
            .obtenerEstudiantes()
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

    fun seleccionarEstudiante(estudiante: Usuario) {
        _uiState.update {
            it.copy(
                estudianteId = estudiante.id,
                estudianteTexto = "${estudiante.nombre} ${estudiante.apellido}",
                errorEstudiante = null
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
        if (!validarFormulario(estado)) return

        val sectorId = estado.sectorId ?: return
        val estudianteId = estado.estudianteId ?: return

        val cultivo = Cultivo(
            nombre = estado.nombre.trim(),
            sectorId = sectorId,
            fechaSiembra = estado.fechaSiembra.trim(),
            fechaCosecha = estado.fechaCosecha.trim(),
            frecuenciaRiegoDias = estado.frecuenciaRiego.toInt(),
            estado = estado.estado.name,
            estudianteId = estudianteId
        )
        viewModelScope.launch {
            repository.guardar(cultivo)
            _uiState.value = CultivoUiState(mensaje = "Cultivo guardado correctamente")
        }
    }

    // Marca los errores en pantalla y devuelve true si el formulario se puede guardar
    private fun validarFormulario(estado: CultivoUiState): Boolean {
        val rNombre = Validaciones.validarNombre(estado.nombre, "El nombre del cultivo")
        val rSiembra = Validaciones.validarFecha(estado.fechaSiembra, "La fecha de siembra")
        val rCosecha = Validaciones.validarCosechaPosterior(
            estado.fechaSiembra, estado.fechaCosecha
        )
        val rRiego = Validaciones.validarFrecuenciaRiego(estado.frecuenciaRiego)

        _uiState.update {
            it.copy(
                errorNombre = rNombre.mensaje,
                errorSector = if (estado.sectorId == null) "Selecciona un sector" else null,
                errorEstudiante = if (estado.estudianteId == null) "Selecciona un estudiante" else null,
                errorSiembra = rSiembra.mensaje,
                errorCosecha = rCosecha.mensaje,
                errorRiego = rRiego.mensaje,
                mensaje = ""
            )
        }

        return rNombre.esValido && rSiembra.esValido &&
                rCosecha.esValido && rRiego.esValido &&
                estado.sectorId != null && estado.estudianteId != null
    }
}
