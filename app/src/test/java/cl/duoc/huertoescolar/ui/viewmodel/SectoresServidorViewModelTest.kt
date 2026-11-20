package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.SectoresRemoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.coroutines.cancellation.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SectoresServidorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mensajeDeError =
        "No se pudo conectar con el servidor. Comprueba la conexión e inténtalo nuevamente."

    private fun dto(id: Int) = SectorDto(
        id = id,
        codigo = "S$id",
        nombre = "Sector $id",
        ubicacion = "Patio",
        estado = "DISPONIBLE",
        fechaCreacion = "01/01/2026"
    )

    @Test
    fun init_cuandoElServidorResponde_muestraLosSectores() {
        // ARRANGE
        val lista = listOf(dto(1), dto(2))
        val repositorio = mockk<SectoresRemoteRepository>()
        coEvery { repositorio.obtenerSectores() } returns lista

        // ACT
        val viewModel = SectoresServidorViewModel(repositorio)

        // ASSERT
        assertEquals(SectoresServidorUiState.Exito(lista), viewModel.uiState.value)
    }

    @Test
    fun init_cuandoElServidorFalla_muestraMensajeDeError() {
        // ARRANGE
        val repositorio = mockk<SectoresRemoteRepository>()
        coEvery { repositorio.obtenerSectores() } throws RuntimeException("sin red")

        // ACT
        val viewModel = SectoresServidorViewModel(repositorio)

        // ASSERT
        assertEquals(SectoresServidorUiState.Error(mensajeDeError), viewModel.uiState.value)
    }

    @Test
    fun cargarSectores_despuesDeUnError_vuelveAIntentarYMuestraLosSectores() {
        // ARRANGE: primera llamada falla, la segunda funciona
        val lista = listOf(dto(1))
        val repositorio = mockk<SectoresRemoteRepository>()
        coEvery { repositorio.obtenerSectores() } throws RuntimeException("sin red") andThen lista
        val viewModel = SectoresServidorViewModel(repositorio)
        assertEquals(SectoresServidorUiState.Error(mensajeDeError), viewModel.uiState.value)

        // ACT
        viewModel.cargarSectores()

        // ASSERT
        assertEquals(SectoresServidorUiState.Exito(lista), viewModel.uiState.value)
    }

    @Test
    fun init_siLaCorrutinaSeCancela_noLoTrataComoErrorDeRed() {
        // ARRANGE
        val repositorio = mockk<SectoresRemoteRepository>()
        coEvery { repositorio.obtenerSectores() } throws CancellationException("cancelado")

        // ACT
        val viewModel = SectoresServidorViewModel(repositorio)

        // ASSERT: la cancelación se relanza, así que nunca llega a mostrarse como error
        assertEquals(SectoresServidorUiState.Cargando, viewModel.uiState.value)
    }

    @Test
    fun init_alCrearseElViewModel_consultaElServidorUnaSolaVez() {
        // ARRANGE
        val repositorio = mockk<SectoresRemoteRepository>()
        coEvery { repositorio.obtenerSectores() } returns emptyList()

        // ACT
        SectoresServidorViewModel(repositorio)

        // ASSERT
        coVerify(exactly = 1) { repositorio.obtenerSectores() }
    }
}
