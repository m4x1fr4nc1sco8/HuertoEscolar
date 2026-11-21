package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import cl.duoc.huertoescolar.errorHttp
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SincronizacionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var usuarios: UsuarioRepository
    private lateinit var sectores: SectorRepository
    private lateinit var cultivos: CultivoRepository
    private lateinit var viewModel: SincronizacionViewModel

    @Before
    fun preparar() {
        // SesionActual es un object (estado global): se limpia para que un test no afecte a otro
        SesionActual.cerrar()
        usuarios = mockk()
        sectores = mockk()
        cultivos = mockk()
        coEvery { usuarios.sincronizar() } just Runs
        coEvery { sectores.sincronizar() } just Runs
        coEvery { cultivos.sincronizar() } just Runs
        viewModel = SincronizacionViewModel(usuarios, sectores, cultivos)
    }

    @After
    fun limpiar() {
        SesionActual.cerrar()
    }

    private fun usuario(id: Int) = Usuario(
        id = id,
        nombre = "Ana",
        apellido = "Soto",
        correo = "ana@huerto.cl",
        fechaNacimiento = "01/01/2012",
        clave = "12345678",
        tipoUsuario = Rol.ESTUDIANTE.name
    )

    @Test
    fun estado_alCrearseElViewModel_esInactivo() {
        assertEquals(EstadoSincronizacion.Inactivo, viewModel.estado.value)
    }

    @Test
    fun sincronizar_servidorResponde_descargaTodoYTerminaCompleta() {
        // ACT
        viewModel.sincronizar()

        // ASSERT
        coVerify(exactly = 1) { usuarios.sincronizar() }
        coVerify(exactly = 1) { sectores.sincronizar() }
        coVerify(exactly = 1) { cultivos.sincronizar() }
        assertEquals(EstadoSincronizacion.Completa, viewModel.estado.value)
    }

    @Test
    fun sincronizar_sinConexion_avisaYNoDescargaSectoresNiCultivos() {
        // ARRANGE
        coEvery { usuarios.sincronizar() } throws IOException("sin red")

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(
            EstadoSincronizacion.SinConexion("No se pudo conectar con el servidor. Comprueba la conexión."),
            viewModel.estado.value
        )
        coVerify(exactly = 0) { sectores.sincronizar() }
        coVerify(exactly = 0) { cultivos.sincronizar() }
    }

    @Test
    fun sincronizar_servidorRespondeConError_muestraElMensajeDelServidor() {
        // ARRANGE
        coEvery { sectores.sincronizar() } throws errorHttp(500, "Error interno")

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(EstadoSincronizacion.SinConexion("Error interno"), viewModel.estado.value)
    }

    @Test
    fun sincronizar_despuesDeUnFallo_sePuedeReintentar() {
        // ARRANGE
        coEvery { usuarios.sincronizar() } throws IOException("sin red") andThen Unit
        viewModel.sincronizar()

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(EstadoSincronizacion.Completa, viewModel.estado.value)
        coVerify(exactly = 2) { usuarios.sincronizar() }
    }

    @Test
    fun sincronizar_conUsuarioConectado_actualizaLaSesionConLosDatosDeRoom() {
        // ARRANGE: la id local (1) cambió a la del servidor (5) tras sincronizar
        SesionActual.iniciar(usuario(id = 1))
        coEvery { usuarios.buscarPorCorreo("ana@huerto.cl") } returns usuario(id = 5)

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(5, SesionActual.usuario.value?.id)
    }

    @Test
    fun sincronizar_conUsuarioSinCambios_noReemplazaLaSesion() {
        // ARRANGE
        val conectado = usuario(id = 1)
        SesionActual.iniciar(conectado)
        coEvery { usuarios.buscarPorCorreo("ana@huerto.cl") } returns conectado.copy()

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(conectado, SesionActual.usuario.value)
    }

    @Test
    fun sincronizar_siElUsuarioConectadoYaNoExiste_dejaLaSesionIgual() {
        // ARRANGE
        val conectado = usuario(id = 1)
        SesionActual.iniciar(conectado)
        coEvery { usuarios.buscarPorCorreo("ana@huerto.cl") } returns null

        // ACT
        viewModel.sincronizar()

        // ASSERT
        assertEquals(conectado, SesionActual.usuario.value)
    }

    @Test
    fun sincronizar_sinSesionIniciada_noBuscaAlUsuario() {
        // ACT
        viewModel.sincronizar()

        // ASSERT
        coVerify(exactly = 0) { usuarios.buscarPorCorreo(any()) }
    }

    @Test
    fun sincronizar_siSeCancela_noLoTrataComoErrorYNoLanzaOtraSincronizacion() {
        // ARRANGE
        coEvery { usuarios.sincronizar() } throws CancellationException("cancelado")

        // ACT
        viewModel.sincronizar()
        viewModel.sincronizar()

        // ASSERT: sigue "sincronizando", por eso la segunda llamada no hace nada
        assertEquals(EstadoSincronizacion.Sincronizando, viewModel.estado.value)
        coVerify(exactly = 1) { usuarios.sincronizar() }
    }
}
