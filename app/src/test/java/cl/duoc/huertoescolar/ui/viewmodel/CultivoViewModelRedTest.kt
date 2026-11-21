package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.model.Usuario
import cl.duoc.huertoescolar.errorHttp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Qué pasa en el formulario de cultivo cuando el servidor falla
class CultivoViewModelRedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var cultivoRepo: CultivoRepository
    private lateinit var viewModel: CultivoViewModel

    private val sector = Sector(1, "S1", "Huerta Norte", "Patio", "DISPONIBLE", "01/01/2026")
    private val estudiante = Usuario(
        id = 5, nombre = "Ana", apellido = "Soto", correo = "ana@huerto.cl",
        fechaNacimiento = "01/01/2012", clave = "12345678", tipoUsuario = Rol.ESTUDIANTE.name
    )

    @Before
    fun preparar() {
        cultivoRepo = mockk()
        val sectorRepo = mockk<SectorRepository>()
        val usuarioRepo = mockk<UsuarioRepository>()
        every { sectorRepo.obtenerSectores() } returns flowOf(listOf(sector))
        every { usuarioRepo.obtenerEstudiantes() } returns flowOf(listOf(estudiante))
        viewModel = CultivoViewModel(cultivoRepo, sectorRepo, usuarioRepo)

        viewModel.cambiarNombre("Tomates")
        viewModel.seleccionarSector(sector)
        viewModel.seleccionarEstudiante(estudiante)
        viewModel.cambiarSiembra("01/03/2026")
        viewModel.cambiarCosecha("01/06/2026")
        viewModel.cambiarLimite("15/05/2026")
        viewModel.cambiarRiego("3")
    }

    @Test
    fun guardar_sinConexionConElServidor_avisaYConservaElFormulario() {
        // ARRANGE
        coEvery { cultivoRepo.guardar(any()) } throws IOException("sin red")

        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("No se pudo conectar con el servidor. Comprueba la conexión.", estado.mensaje)
        assertEquals("Tomates", estado.nombre)
        assertEquals(1, estado.sectorId)
    }

    @Test
    fun guardar_servidorRechazaElCultivo_muestraElMensajeDelServidor() {
        // ARRANGE
        coEvery { cultivoRepo.guardar(any()) } throws
                errorHttp(400, "La cosecha debe ser posterior a la siembra")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("La cosecha debe ser posterior a la siembra", viewModel.uiState.value.mensaje)
    }
}
