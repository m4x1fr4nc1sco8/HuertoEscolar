package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.errorHttp
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Qué pasa en el formulario de sector cuando el servidor falla
class SectorViewModelRedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: SectorRepository
    private lateinit var viewModel: SectorViewModel

    @Before
    fun preparar() {
        repositorio = mockk()
        coEvery { repositorio.existeCodigo(any()) } returns false
        coEvery { repositorio.existeCodigoEnOtroSector(any(), any()) } returns false
        viewModel = SectorViewModel(
            repository = repositorio,
            guardarFoto = { null },
            fechaHoy = { "18/11/2026" }
        )
    }

    private fun llenarFormularioValido() {
        viewModel.cambiarCodigo("S1")
        viewModel.cambiarNombre("Huerta Norte")
        viewModel.cambiarUbicacion("Patio central")
        viewModel.cambiarConfirma(true)
    }

    private val sectorExistente = Sector(
        id = 7,
        codigo = "S7",
        nombre = "Invernadero",
        ubicacion = "Costado sur",
        estado = "EN_USO",
        fechaCreacion = "01/01/2026"
    )

    @Test
    fun guardar_sinConexionConElServidor_avisaYConservaElFormulario() {
        // ARRANGE
        coEvery { repositorio.guardar(any()) } throws IOException("sin red")
        llenarFormularioValido()

        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("No se pudo conectar con el servidor. Comprueba la conexión.", estado.mensaje)
        assertEquals("S1", estado.codigo)
        assertEquals("Huerta Norte", estado.nombre)
    }

    @Test
    fun guardar_servidorRechazaElCodigo_muestraElMensajeDelServidor() {
        // ARRANGE
        coEvery { repositorio.guardar(any()) } throws errorHttp(409, "Ya existe un sector con ese código")
        llenarFormularioValido()

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Ya existe un sector con ese código", viewModel.uiState.value.mensaje)
    }

    @Test
    fun guardar_editandoYElServidorRechaza_muestraElMensajeYNoMarcaGuardado() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente
        coEvery {
            repositorio.actualizar(any(), any(), any(), any(), any(), any())
        } throws errorHttp(404, "No se encontró el sector 7")
        viewModel.cargarParaEditar(7)
        viewModel.cambiarConfirma(true)

        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("No se encontró el sector 7", estado.mensaje)
        assertFalse(estado.guardado)
    }
}
