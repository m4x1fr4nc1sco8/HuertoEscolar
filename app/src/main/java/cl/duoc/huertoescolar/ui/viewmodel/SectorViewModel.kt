package cl.duoc.huertoescolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.validation.Validaciones
import cl.duoc.huertoescolar.util.fechaActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SectorViewModel(
    private val repository: SectorRepository,
    // Copia una foto temporal a almacenamiento permanente (null si falla)
    private val guardarFoto: suspend (String) -> String?,
    private val fechaHoy: () -> String = ::fechaActual
) : ViewModel() {

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

    fun cambiarEstado(valor: EstadoSector) {
        _uiState.update { it.copy(estado = valor) }
    }

    // Carga un sector existente en el formulario para editarlo
    fun cargarParaEditar(id: Int) {
        if (_uiState.value.id == id) return
        viewModelScope.launch {
            val sector = repository.obtenerPorId(id)
            if (sector == null) {
                _uiState.update { it.copy(mensaje = "No se encontró el sector") }
                return@launch
            }
            _uiState.value = SectorUiState(
                id = sector.id,
                codigo = sector.codigo,
                nombre = sector.nombre,
                ubicacion = sector.ubicacion,
                estado = EstadoSector.entries.firstOrNull { it.name == sector.estado }
                    ?: EstadoSector.DISPONIBLE,
                fotoUri = sector.fotoUri
            )
        }
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
        if (!validarFormulario(estado)) return

        val estadoSector = estadoDelSector(estado)
        val fecha = fechaHoy()

        viewModelScope.launch {
            if (existeCodigoDuplicado(estado)) {
                _uiState.update { it.copy(errorCodigo = "Ya existe un sector con ese código") }
                return@launch
            }
            val rutaFoto = estado.fotoUri?.let { prepararFoto(it) }

            if (estado.id != null) {
                actualizarSector(estado, estado.id, estadoSector, rutaFoto)
            } else {
                crearSector(estado, estadoSector, fecha, rutaFoto)
            }
        }
    }

    // Marca los errores en pantalla y devuelve true si el formulario se puede guardar
    private fun validarFormulario(estado: SectorUiState): Boolean {
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

        return rCodigo.esValido && rNombre.esValido &&
                rUbicacion.esValido && estado.confirma
    }

    // Al editar se conserva el estado elegido; al crear depende del switch "en uso"
    private fun estadoDelSector(estado: SectorUiState): EstadoSector = when {
        estado.id != null -> estado.estado
        estado.enUso -> EstadoSector.EN_USO
        else -> EstadoSector.DISPONIBLE
    }

    private suspend fun existeCodigoDuplicado(estado: SectorUiState): Boolean {
        val codigo = estado.codigo.trim()
        return if (estado.id != null) {
            repository.existeCodigoEnOtroSector(codigo, estado.id)
        } else {
            repository.existeCodigo(codigo)
        }
    }

    // Si ya es una foto guardada del sector, se conserva tal cual
    private suspend fun prepararFoto(temporal: String): String? =
        if (temporal.startsWith("file:")) temporal else guardarFoto(temporal)

    private suspend fun actualizarSector(
        estado: SectorUiState,
        id: Int,
        estadoSector: EstadoSector,
        rutaFoto: String?
    ) {
        val ok = repository.actualizar(
            id = id,
            codigo = estado.codigo.trim(),
            nombre = estado.nombre.trim(),
            ubicacion = estado.ubicacion.trim(),
            estado = estadoSector.name,
            fotoUri = rutaFoto
        )
        _uiState.update {
            if (ok) {
                it.copy(mensaje = "Sector actualizado correctamente", guardado = true)
            } else {
                it.copy(mensaje = "No se pudo actualizar el sector")
            }
        }
    }

    private suspend fun crearSector(
        estado: SectorUiState,
        estadoSector: EstadoSector,
        fecha: String,
        rutaFoto: String?
    ) {
        repository.guardar(
            Sector(
                codigo = estado.codigo.trim(),
                nombre = estado.nombre.trim(),
                ubicacion = estado.ubicacion.trim(),
                estado = estadoSector.name,
                fechaCreacion = fecha,
                fotoUri = rutaFoto
            )
        )
        _uiState.value = SectorUiState(mensaje = "Sector guardado correctamente")
    }
}
