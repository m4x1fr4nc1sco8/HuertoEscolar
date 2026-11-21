package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.ClimaRepository
import cl.duoc.huertoescolar.domain.model.Clima
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ClimaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: ClimaRepository

    private val clima = Clima(
        temperatura = 18.0,
        humedad = 60,
        vientoKmH = 10.0,
        descripcion = "Despejado",
        icono = "☀️",
        pronostico = emptyList()
    )

    private val mensajeDeError = "No se pudo obtener el clima. Comprueba la conexión."

    @Before
    fun preparar() {
        repositorio = mockk()
    }

    @Test
    fun init_apiResponde_muestraElClima() {
        // ARRANGE
        coEvery { repositorio.obtenerClima() } returns clima

        // ACT
        val viewModel = ClimaViewModel(repositorio)

        // ASSERT
        assertEquals(ClimaUiState.Exito(clima), viewModel.estado.value)
    }

    @Test
    fun init_apiFalla_muestraMensajeDeError() {
        // ARRANGE
        coEvery { repositorio.obtenerClima() } throws IOException("sin red")

        // ACT
        val viewModel = ClimaViewModel(repositorio)

        // ASSERT
        assertEquals(ClimaUiState.Error(mensajeDeError), viewModel.estado.value)
    }

    @Test
    fun cargar_despuesDeUnError_vuelveAIntentarYMuestraElClima() {
        // ARRANGE: la primera llamada falla y la segunda funciona
        coEvery { repositorio.obtenerClima() } throws IOException("sin red") andThen clima
        val viewModel = ClimaViewModel(repositorio)
        assertEquals(ClimaUiState.Error(mensajeDeError), viewModel.estado.value)

        // ACT
        viewModel.cargar()

        // ASSERT
        assertEquals(ClimaUiState.Exito(clima), viewModel.estado.value)
        coVerify(exactly = 2) { repositorio.obtenerClima() }
    }

    @Test
    fun init_siSeCancela_noLoTrataComoError() {
        // ARRANGE
        coEvery { repositorio.obtenerClima() } throws CancellationException("cancelado")

        // ACT
        val viewModel = ClimaViewModel(repositorio)

        // ASSERT: la cancelación se relanza, así que nunca llega a mostrarse como error
        assertEquals(ClimaUiState.Cargando, viewModel.estado.value)
    }
}
