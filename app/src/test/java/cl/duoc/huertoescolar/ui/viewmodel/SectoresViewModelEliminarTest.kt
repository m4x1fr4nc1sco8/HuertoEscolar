package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.errorHttp
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SectoresViewModelEliminarTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: SectorRepository
    private lateinit var viewModel: SectoresViewModel

    @Before
    fun preparar() {
        repositorio = mockk()
        every { repositorio.obtenerSectores() } returns flowOf(emptyList())
        viewModel = SectoresViewModel(repositorio)
    }

    @Test
    fun mensaje_alCrearseElViewModel_esNull() {
        assertNull(viewModel.mensaje.value)
    }

    @Test
    fun eliminar_servidorElimina_avisaQueSeElimino() {
        // ARRANGE
        coEvery { repositorio.eliminar(3) } just Runs

        // ACT
        viewModel.eliminar(3)

        // ASSERT
        coVerify(exactly = 1) { repositorio.eliminar(3) }
        assertEquals("Sector eliminado.", viewModel.mensaje.value)
    }

    @Test
    fun eliminar_sectorConCultivos_muestraElMensajeDelServidor() {
        // ARRANGE
        coEvery { repositorio.eliminar(3) } throws
                errorHttp(409, "No se puede eliminar: el sector tiene cultivos asociados")

        // ACT
        viewModel.eliminar(3)

        // ASSERT
        assertEquals(
            "No se puede eliminar: el sector tiene cultivos asociados",
            viewModel.mensaje.value
        )
    }

    @Test
    fun eliminar_sinConexion_avisaQueNoHayConexion() {
        // ARRANGE
        coEvery { repositorio.eliminar(3) } throws IOException("sin red")

        // ACT
        viewModel.eliminar(3)

        // ASSERT
        assertEquals(
            "No se pudo conectar con el servidor. Comprueba la conexión.",
            viewModel.mensaje.value
        )
    }

    @Test
    fun mensajeMostrado_conUnMensajePendiente_loLimpia() {
        // ARRANGE
        coEvery { repositorio.eliminar(3) } just Runs
        viewModel.eliminar(3)

        // ACT
        viewModel.mensajeMostrado()

        // ASSERT
        assertNull(viewModel.mensaje.value)
    }
}
