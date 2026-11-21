package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Acciones de los cultivos contra el servidor: errores de red y eliminar
class CultivosViewModelRedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sinConexion = "No se pudo conectar con el servidor. Comprueba la conexión."

    private lateinit var repositorio: CultivoRepository
    private lateinit var viewModel: CultivosViewModel

    @Before
    fun preparar() {
        // SesionActual es un object (estado global): se limpia para que un test no afecte a otro
        SesionActual.cerrar()
        repositorio = mockk()
        viewModel = CultivosViewModel(repositorio)
    }

    @After
    fun limpiar() {
        SesionActual.cerrar()
    }

    private fun usuario(id: Int, rol: Rol) = Usuario(
        id = id, nombre = "Ana", apellido = "Soto", correo = "ana$id@huerto.cl",
        fechaNacimiento = "01/01/2000", clave = "12345678", tipoUsuario = rol.name
    )

    private fun iniciarComoAdmin() = SesionActual.iniciar(usuario(1, Rol.ADMINISTRADOR))

    private fun iniciarComoEstudiante() = SesionActual.iniciar(usuario(3, Rol.ESTUDIANTE))

    // ---------- errores de red ----------

    @Test
    fun marcarComoRealizada_sinConexion_avisaQueNoHayConexion() {
        // ARRANGE
        iniciarComoEstudiante()
        coEvery { repositorio.marcarComoRealizada(any(), any(), any(), any()) } throws IOException("sin red")

        // ACT
        viewModel.marcarComoRealizada(10, "file:///foto.jpg", "Regué")

        // ASSERT
        assertEquals(sinConexion, viewModel.mensaje.value)
    }

    @Test
    fun cambiarEstado_servidorRechaza_muestraElMensajeDelServidor() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.cambiarEstado(any(), any()) } throws
                errorHttp(404, "No se encontró el cultivo 10")

        // ACT
        viewModel.cambiarEstado(10, EstadoCultivo.COSECHADO)

        // ASSERT
        assertEquals("No se encontró el cultivo 10", viewModel.mensaje.value)
    }

    @Test
    fun validar_sinConexion_avisaQueNoHayConexion() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.validar(10) } throws IOException("sin red")

        // ACT
        viewModel.validar(10)

        // ASSERT
        assertEquals(sinConexion, viewModel.mensaje.value)
    }

    @Test
    fun rechazar_servidorRespondeQueLaTareaNoEstaPendiente_muestraElMensajeDelServidor() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.rechazar(any(), any()) } throws
                errorHttp(409, "La tarea no está pendiente de validación")

        // ACT
        val valido = viewModel.rechazar(10, "Foto borrosa")

        // ASSERT
        assertEquals(true, valido)
        assertEquals("La tarea no está pendiente de validación", viewModel.mensaje.value)
    }

    // ---------- eliminar ----------

    @Test
    fun eliminar_siEsAdministrador_eliminaYAvisa() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.eliminar(10) } just Runs

        // ACT
        viewModel.eliminar(10)

        // ASSERT
        coVerify(exactly = 1) { repositorio.eliminar(10) }
        assertEquals("Cultivo eliminado.", viewModel.mensaje.value)
    }

    @Test
    fun eliminar_siNoEsAdministrador_noHaceNada() {
        // ARRANGE
        iniciarComoEstudiante()

        // ACT
        viewModel.eliminar(10)

        // ASSERT
        coVerify(exactly = 0) { repositorio.eliminar(any()) }
        assertNull(viewModel.mensaje.value)
    }

    @Test
    fun eliminar_sinConexion_avisaQueNoHayConexion() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.eliminar(10) } throws IOException("sin red")

        // ACT
        viewModel.eliminar(10)

        // ASSERT
        assertEquals(sinConexion, viewModel.mensaje.value)
    }
}
