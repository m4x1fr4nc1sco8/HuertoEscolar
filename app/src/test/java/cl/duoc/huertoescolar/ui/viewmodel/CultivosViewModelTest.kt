package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CultivosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        id = id,
        nombre = "Ana",
        apellido = "Soto",
        correo = "ana$id@huerto.cl",
        fechaNacimiento = "01/01/2000",
        clave = "12345678",
        tipoUsuario = rol.name
    )

    private fun cultivo(id: Int) = Cultivo(
        id = id,
        nombre = "Cultivo $id",
        sectorId = 1,
        fechaSiembra = "01/03/2026",
        fechaCosecha = "01/06/2026",
        frecuenciaRiegoDias = 3,
        estado = EstadoCultivo.EN_CRECIMIENTO.name
    )

    private fun iniciarComoAdmin() = SesionActual.iniciar(usuario(1, Rol.ADMINISTRADOR))

    private fun iniciarComoEstudiante() = SesionActual.iniciar(usuario(3, Rol.ESTUDIANTE))

    // ---------- lista de cultivos según el rol ----------

    @Test
    fun cultivos_sinNingunSuscriptor_empiezaConListaVacia() {
        assertEquals(emptyList<Cultivo>(), viewModel.cultivos.value)
    }

    @Test
    fun cultivos_siEsAdministrador_muestraTodosLosCultivos() = runTest {
        // ARRANGE
        iniciarComoAdmin()
        every { repositorio.obtenerCultivos() } returns flowOf(listOf(cultivo(1), cultivo(2)))

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.cultivos.collect {}
        }

        // ASSERT
        assertEquals(2, viewModel.cultivos.value.size)
        verify(exactly = 0) { repositorio.obtenerPorEstudiante(any()) }
    }

    @Test
    fun cultivos_sinSesion_muestraTodosLosCultivos() = runTest {
        // ARRANGE
        every { repositorio.obtenerCultivos() } returns flowOf(listOf(cultivo(1)))

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.cultivos.collect {}
        }

        // ASSERT
        assertEquals(1, viewModel.cultivos.value.size)
    }

    @Test
    fun cultivos_siEsEstudiante_muestraSoloLosAsignados() = runTest {
        // ARRANGE
        iniciarComoEstudiante()
        every { repositorio.obtenerPorEstudiante(3) } returns flowOf(listOf(cultivo(9)))

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.cultivos.collect {}
        }

        // ASSERT
        assertEquals(listOf(9), viewModel.cultivos.value.map { it.id })
        verify(exactly = 0) { repositorio.obtenerCultivos() }
    }

    // ---------- validación del comentario ----------

    @Test
    fun validarComentario_comentarioVacio_retornaFalseYMarcaError() {
        // ACT
        val resultado = viewModel.validarComentario("   ")

        // ASSERT
        assertFalse(resultado)
        assertEquals("El comentario es obligatorio", viewModel.errorComentario.value)
    }

    @Test
    fun validarComentario_comentarioCorrecto_retornaTrueYSinError() {
        // ARRANGE: primero hay un error previo
        viewModel.validarComentario("")

        // ACT
        val resultado = viewModel.validarComentario("Regué y quité las malezas")

        // ASSERT
        assertTrue(resultado)
        assertNull(viewModel.errorComentario.value)
    }

    @Test
    fun limpiarErrorComentario_conErrorPrevio_loBorra() {
        // ARRANGE
        viewModel.validarComentario("")

        // ACT
        viewModel.limpiarErrorComentario()

        // ASSERT
        assertNull(viewModel.errorComentario.value)
    }

    // ---------- estudiante envía la tarea ----------

    @Test
    fun marcarComoRealizada_sinSesion_noHaceNada() {
        // ACT
        viewModel.marcarComoRealizada(10, "file:///foto.jpg", "Listo")

        // ASSERT
        coVerify(exactly = 0) { repositorio.marcarComoRealizada(any(), any(), any(), any()) }
        assertNull(viewModel.mensaje.value)
    }

    @Test
    fun marcarComoRealizada_comentarioEnBlanco_avisaYNoEnvia() {
        // ARRANGE
        iniciarComoEstudiante()

        // ACT
        viewModel.marcarComoRealizada(10, "file:///foto.jpg", "   ")

        // ASSERT
        assertEquals("Escribe un comentario para enviar la tarea.", viewModel.mensaje.value)
        coVerify(exactly = 0) { repositorio.marcarComoRealizada(any(), any(), any(), any()) }
    }

    @Test
    fun marcarComoRealizada_cuandoSeRegistra_avisaQueQuedaPendienteDeValidacion() {
        // ARRANGE
        iniciarComoEstudiante()
        coEvery {
            repositorio.marcarComoRealizada(10, 3, "file:///foto.jpg", "Regué")
        } returns true

        // ACT
        viewModel.marcarComoRealizada(10, "file:///foto.jpg", "Regué")

        // ASSERT
        assertEquals("Tarea enviada. Queda pendiente de validación.", viewModel.mensaje.value)
    }

    @Test
    fun marcarComoRealizada_cuandoElRepositorioFalla_avisaQueNoSePudoEnviar() {
        // ARRANGE
        iniciarComoEstudiante()
        coEvery { repositorio.marcarComoRealizada(any(), any(), any(), any()) } returns false

        // ACT
        viewModel.marcarComoRealizada(10, "file:///foto.jpg", "Regué")

        // ASSERT
        assertEquals("No se pudo enviar la tarea.", viewModel.mensaje.value)
    }

    // ---------- administrador cambia el estado ----------

    @Test
    fun cambiarEstado_siNoEsAdministrador_noHaceNada() {
        // ARRANGE
        iniciarComoEstudiante()

        // ACT
        viewModel.cambiarEstado(10, EstadoCultivo.COSECHADO)

        // ASSERT
        coVerify(exactly = 0) { repositorio.cambiarEstado(any(), any()) }
        assertNull(viewModel.mensaje.value)
    }

    @Test
    fun cambiarEstado_siEsAdministrador_actualizaYAvisaConElNuevoEstado() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.cambiarEstado(10, EstadoCultivo.LISTO_PARA_COSECHAR) } returns true

        // ACT
        viewModel.cambiarEstado(10, EstadoCultivo.LISTO_PARA_COSECHAR)

        // ASSERT
        assertEquals("Estado actualizado a: listo para cosechar", viewModel.mensaje.value)
    }

    @Test
    fun cambiarEstado_cuandoElRepositorioFalla_avisaQueNoSePudo() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.cambiarEstado(any(), any()) } returns false

        // ACT
        viewModel.cambiarEstado(10, EstadoCultivo.COSECHADO)

        // ASSERT
        assertEquals("No se pudo actualizar el estado.", viewModel.mensaje.value)
    }

    // ---------- administrador valida ----------

    @Test
    fun validar_siNoEsAdministrador_noHaceNada() {
        // ARRANGE
        iniciarComoEstudiante()

        // ACT
        viewModel.validar(10)

        // ASSERT
        coVerify(exactly = 0) { repositorio.validar(any()) }
    }

    @Test
    fun validar_siEsAdministrador_avisaQueLaTareaFueValidada() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.validar(10) } returns true

        // ACT
        viewModel.validar(10)

        // ASSERT
        assertEquals("Tarea validada.", viewModel.mensaje.value)
    }

    @Test
    fun validar_cuandoElRepositorioFalla_avisaQueNoSePudo() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.validar(10) } returns false

        // ACT
        viewModel.validar(10)

        // ASSERT
        assertEquals("No se pudo validar la tarea.", viewModel.mensaje.value)
    }

    // ---------- administrador rechaza ----------

    @Test
    fun rechazar_siNoEsAdministrador_retornaFalseYNoLlamaAlRepositorio() {
        // ARRANGE
        iniciarComoEstudiante()

        // ACT
        val resultado = viewModel.rechazar(10, "Foto borrosa")

        // ASSERT
        assertFalse(resultado)
        coVerify(exactly = 0) { repositorio.rechazar(any(), any()) }
    }

    @Test
    fun rechazar_motivoVacio_retornaFalseYMarcaErrorDelMotivo() {
        // ARRANGE
        iniciarComoAdmin()

        // ACT
        val resultado = viewModel.rechazar(10, "  ")

        // ASSERT
        assertFalse(resultado)
        assertEquals("Escribe el motivo del rechazo", viewModel.errorMotivo.value)
        coVerify(exactly = 0) { repositorio.rechazar(any(), any()) }
    }

    @Test
    fun rechazar_conMotivoValido_retornaTrueYEnviaElRechazo() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.rechazar(10, "Foto borrosa") } returns true

        // ACT
        val resultado = viewModel.rechazar(10, "Foto borrosa")

        // ASSERT
        assertTrue(resultado)
        assertNull(viewModel.errorMotivo.value)
        assertEquals("Tarea rechazada. El estudiante podrá reenviarla.", viewModel.mensaje.value)
    }

    @Test
    fun rechazar_cuandoElRepositorioFalla_avisaQueNoSePudo() {
        // ARRANGE
        iniciarComoAdmin()
        coEvery { repositorio.rechazar(any(), any()) } returns false

        // ACT
        val resultado = viewModel.rechazar(10, "Foto borrosa")

        // ASSERT: el motivo era válido, por eso retorna true aunque el guardado falle
        assertTrue(resultado)
        assertEquals("No se pudo rechazar la tarea.", viewModel.mensaje.value)
    }

    @Test
    fun limpiarErrorMotivo_conErrorPrevio_loBorra() {
        // ARRANGE
        iniciarComoAdmin()
        viewModel.rechazar(10, "")

        // ACT
        viewModel.limpiarErrorMotivo()

        // ASSERT
        assertNull(viewModel.errorMotivo.value)
    }

    // ---------- mensajes ----------

    @Test
    fun informar_conUnTexto_loPublicaComoMensaje() {
        // ACT
        viewModel.informar("Permiso de cámara denegado")

        // ASSERT
        assertEquals("Permiso de cámara denegado", viewModel.mensaje.value)
    }

    @Test
    fun mensajeMostrado_conUnMensajePendiente_loLimpia() {
        // ARRANGE
        viewModel.informar("Hola")

        // ACT
        viewModel.mensajeMostrado()

        // ASSERT
        assertNull(viewModel.mensaje.value)
    }
}
