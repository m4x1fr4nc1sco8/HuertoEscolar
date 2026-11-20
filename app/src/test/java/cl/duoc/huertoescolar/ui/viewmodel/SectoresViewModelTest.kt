package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.domain.model.Sector
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SectoresViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun sector(id: Int, codigo: String) = Sector(
        id = id,
        codigo = codigo,
        nombre = "Sector $codigo",
        ubicacion = "Patio",
        estado = "DISPONIBLE",
        fechaCreacion = "01/01/2026"
    )

    @Test
    fun sectores_sinNingunSuscriptor_empiezaConListaVacia() {
        // ARRANGE
        val repositorio = mockk<SectorRepository>()
        every { repositorio.obtenerSectores() } returns flowOf(listOf(sector(1, "S1")))

        // ACT
        val viewModel = SectoresViewModel(repositorio)

        // ASSERT
        assertEquals(emptyList<Sector>(), viewModel.sectores.value)
    }

    @Test
    fun sectores_conUnSuscriptor_emiteLosSectoresDelRepositorio() = runTest {
        // ARRANGE
        val lista = listOf(sector(1, "S1"), sector(2, "S2"))
        val repositorio = mockk<SectorRepository>()
        every { repositorio.obtenerSectores() } returns flowOf(lista)
        val viewModel = SectoresViewModel(repositorio)

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sectores.collect {}
        }

        // ASSERT
        assertEquals(lista, viewModel.sectores.value)
    }

    @Test
    fun sectores_cuandoElRepositorioCambia_seActualizaLaLista() = runTest {
        // ARRANGE
        val origen = MutableStateFlow(listOf(sector(1, "S1")))
        val repositorio = mockk<SectorRepository>()
        every { repositorio.obtenerSectores() } returns origen
        val viewModel = SectoresViewModel(repositorio)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sectores.collect {}
        }
        assertEquals(1, viewModel.sectores.value.size)

        // ACT
        origen.value = listOf(sector(1, "S1"), sector(2, "S2"))

        // ASSERT
        assertEquals(2, viewModel.sectores.value.size)
    }
}
