package cl.duoc.huertoescolar.ui.viewmodel
import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Alerta
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.TipoAlerta
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: CultivoRepository

    @Before
    fun preparar() {
        // SesionActual es un object (estado global): se limpia para que un test no afecte a otro
        SesionActual.cerrar()
        repositorio = mockk()
    }

    @After
    fun limpiar() {
        SesionActual.cerrar()
    }

    private fun usuario(id: Int, rol: Rol) = Usuario(
        id = id,
        nombre = "Ana",
        apellido = "Soto",
        correo = "ana$id@huerto.cl",
        fechaNacimiento = "01/01/2000",
        clave = "12345678",
        tipoUsuario = rol.name
    )

    // Cosecha en 2020: siempre está vencida sin importar la fecha en que se corran los tests
    private fun cultivoVencido(
        id: Int,
        estado: String = EstadoCultivo.EN_CRECIMIENTO.name
    ) = Cultivo(
        id = id,
        nombre = "Cultivo $id",
        sectorId = 1,
        fechaSiembra = "01/01/2020",
        fechaCosecha = "15/01/2020",
        frecuenciaRiegoDias = 3,
        estado = estado
    )

    private fun TestScope.suscribir(viewModel: AlertasViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alertas.collect {}
        }
    }

    @Test
    fun alertas_sinNingunSuscriptor_empiezaConListaVacia() {
        // ARRANGE
        every { repositorio.obtenerCultivos() } returns flowOf(listOf(cultivoVencido(1)))

        // ACT
        val viewModel = AlertasViewModel(repositorio)

        // ASSERT
        assertEquals(emptyList<Alerta>(), viewModel.alertas.value)
    }

    @Test
    fun alertas_sinSesionIniciada_usaTodosLosCultivos() = runTest {
        // ARRANGE
        every { repositorio.obtenerCultivos() } returns flowOf(listOf(cultivoVencido(1)))
        val viewModel = AlertasViewModel(repositorio)

        // ACT
        suscribir(viewModel)

        // ASSERT
        val alertas = viewModel.alertas.value
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.COSECHA_VENCIDA, alertas[0].tipo)
        assertEquals(1, alertas[0].cultivoId)
    }

    @Test
    fun alertas_siEsAdministrador_usaTodosLosCultivosYNoLosDeUnEstudiante() = runTest {
        // ARRANGE
        SesionActual.iniciar(usuario(1, Rol.ADMINISTRADOR))
        every { repositorio.obtenerCultivos() } returns
                flowOf(listOf(cultivoVencido(1), cultivoVencido(2)))
        val viewModel = AlertasViewModel(repositorio)

        // ACT
        suscribir(viewModel)

        // ASSERT
        assertEquals(2, viewModel.alertas.value.size)
        verify(exactly = 0) { repositorio.obtenerPorEstudiante(any()) }
    }

    @Test
    fun alertas_siEsEstudiante_soloUsaSusCultivosAsignados() = runTest {
        // ARRANGE
        SesionActual.iniciar(usuario(3, Rol.ESTUDIANTE))
        every { repositorio.obtenerPorEstudiante(3) } returns flowOf(listOf(cultivoVencido(7)))
        val viewModel = AlertasViewModel(repositorio)

        // ACT
        suscribir(viewModel)

        // ASSERT
        assertEquals(listOf(7), viewModel.alertas.value.map { it.cultivoId })
        verify(exactly = 0) { repositorio.obtenerCultivos() }
    }

    @Test
    fun alertas_conCultivosCosechadosOFinalizados_noGeneraAlertas() = runTest {
        // ARRANGE
        every { repositorio.obtenerCultivos() } returns flowOf(
            listOf(
                cultivoVencido(1, EstadoCultivo.COSECHADO.name),
                cultivoVencido(2, EstadoCultivo.FINALIZADO.name)
            )
        )
        val viewModel = AlertasViewModel(repositorio)

        // ACT
        suscribir(viewModel)

        // ASSERT
        assertEquals(emptyList<Alerta>(), viewModel.alertas.value)
    }

    @Test
    fun alertas_alCambiarLaSesionAEstudiante_pasaAMostrarSoloSusCultivos() = runTest {
        // ARRANGE: sin sesión se ven todos los cultivos
        every { repositorio.obtenerCultivos() } returns
                flowOf(listOf(cultivoVencido(1), cultivoVencido(2)))
        every { repositorio.obtenerPorEstudiante(3) } returns flowOf(listOf(cultivoVencido(2)))
        val viewModel = AlertasViewModel(repositorio)
        suscribir(viewModel)
        assertEquals(2, viewModel.alertas.value.size)

        // ACT
        SesionActual.iniciar(usuario(3, Rol.ESTUDIANTE))

        // ASSERT
        assertEquals(listOf(2), viewModel.alertas.value.map { it.cultivoId })
    }
}
