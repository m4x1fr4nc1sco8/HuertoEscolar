package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.CultivoDao
import cl.duoc.huertoescolar.data.remote.CultivoDto
import cl.duoc.huertoescolar.data.remote.CultivoSolicitud
import cl.duoc.huertoescolar.data.remote.EnviarTareaSolicitud
import cl.duoc.huertoescolar.data.remote.EstadoSolicitud
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.RechazarTareaSolicitud
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CultivoRepositoryTest {

    private lateinit var dao: CultivoDao
    private lateinit var api: HuertoApi
    private lateinit var repositorio: CultivoRepository

    @Before
    fun preparar() {
        dao = mockk()
        api = mockk()
        repositorio = CultivoRepository(dao, api)
    }

    private fun cultivo(id: Int = 1, estudianteId: Int? = 2) = Cultivo(
        id = id,
        nombre = "Tomate",
        sectorId = 1,
        fechaSiembra = "01/03/2026",
        fechaCosecha = "01/06/2026",
        frecuenciaRiegoDias = 3,
        estado = EstadoCultivo.SEMBRADO.name,
        estudianteId = estudianteId
    )

    private fun dto(id: Int = 1, estudianteId: Int? = 2) = CultivoDto(
        id = id,
        nombre = "Tomate",
        sectorId = 1,
        estudianteId = estudianteId,
        fechaSiembra = "2026-03-01",
        fechaCosecha = "2026-06-01",
        fechaLimite = null,
        frecuenciaRiegoDias = 3,
        estado = "SEMBRADO",
        estadoTarea = "PENDIENTE",
        fotoUrl = null,
        comentarioEstudiante = null,
        motivoRechazo = null
    )

    // ---------- crear ----------

    @Test
    fun guardar_servidorCreaElCultivo_loEspejaEnElDaoConLaIdDelServidor() = runTest {
        // ARRANGE
        val solicitud = slot<CultivoSolicitud>()
        coEvery { api.crearCultivo(capture(solicitud)) } returns dto(id = 9)
        coEvery { dao.insertar(any()) } just Runs

        // ACT
        repositorio.guardar(cultivo(id = 0))

        // ASSERT
        assertEquals(
            CultivoSolicitud("Tomate", 1, 2, "2026-03-01", "2026-06-01", null, 3, "SEMBRADO"),
            solicitud.captured
        )
        coVerify(exactly = 1) { dao.insertar(cultivo(id = 9)) }
    }

    @Test
    fun guardar_servidorNoResponde_lanzaLaExcepcionYNoInsertaEnElDao() {
        // ARRANGE
        coEvery { api.crearCultivo(any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.guardar(cultivo(id = 0)) } }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    // ---------- leer (siempre desde Room) ----------

    @Test
    fun obtenerCultivos_daoConCultivos_emiteLaMismaLista() = runTest {
        // ARRANGE
        val lista = listOf(cultivo(1), cultivo(2))
        every { dao.obtenerCultivos() } returns flowOf(lista)

        // ACT
        val resultado = repositorio.obtenerCultivos().first()

        // ASSERT
        assertEquals(lista, resultado)
    }

    @Test
    fun obtenerPorEstudiante_consultaElDaoConElIdDelEstudiante() = runTest {
        // ARRANGE
        val lista = listOf(cultivo(estudianteId = 7))
        every { dao.obtenerPorEstudiante(7) } returns flowOf(lista)

        // ACT
        val resultado = repositorio.obtenerPorEstudiante(7).first()

        // ASSERT
        assertEquals(lista, resultado)
        verify(exactly = 1) { dao.obtenerPorEstudiante(7) }
    }

    @Test
    fun obtenerPendientesDeValidacion_consultaElDaoPorEstadoDeTarea() = runTest {
        // ARRANGE
        val lista = listOf(cultivo())
        every { dao.obtenerPorEstadoTarea("PENDIENTE_DE_VALIDACION") } returns flowOf(lista)

        // ACT
        val resultado = repositorio.obtenerPendientesDeValidacion().first()

        // ASSERT
        assertEquals(lista, resultado)
        verify(exactly = 1) { dao.obtenerPorEstadoTarea("PENDIENTE_DE_VALIDACION") }
    }

    // ---------- cambiar estado ----------

    @Test
    fun cambiarEstado_cultivoExistente_retornaTrue() = runTest {
        // ARRANGE
        coEvery { api.cambiarEstadoCultivo(1, EstadoSolicitud("COSECHADO")) } returns dto(1)
        coEvery { dao.actualizarEstado(1, "COSECHADO") } returns 1

        // ACT
        val ok = repositorio.cambiarEstado(1, EstadoCultivo.COSECHADO)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun cambiarEstado_cultivoInexistenteEnElDao_retornaFalse() = runTest {
        // ARRANGE
        coEvery { api.cambiarEstadoCultivo(404, any()) } returns dto(404)
        coEvery { dao.actualizarEstado(404, "COSECHADO") } returns 0

        // ACT
        val ok = repositorio.cambiarEstado(404, EstadoCultivo.COSECHADO)

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun cambiarEstado_servidorRechaza_lanzaLaExcepcionYNoTocaElDao() {
        // ARRANGE
        coEvery { api.cambiarEstadoCultivo(any(), any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) {
            runBlocking { repositorio.cambiarEstado(1, EstadoCultivo.COSECHADO) }
        }
        coVerify(exactly = 0) { dao.actualizarEstado(any(), any()) }
    }

    // ---------- eliminar ----------

    @Test
    fun eliminar_servidorElimina_tambienLoBorraDelDao() = runTest {
        // ARRANGE
        coEvery { api.eliminarCultivo(5) } just Runs
        coEvery { dao.eliminar(5) } just Runs

        // ACT
        repositorio.eliminar(5)

        // ASSERT
        coVerifyOrder {
            api.eliminarCultivo(5)
            dao.eliminar(5)
        }
    }

    @Test
    fun eliminar_servidorRechaza_noLoBorraDelDao() {
        // ARRANGE
        coEvery { api.eliminarCultivo(5) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.eliminar(5) } }
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }

    // ---------- tarea: enviar ----------

    @Test
    fun marcarComoRealizada_cultivoDelEstudiante_retornaTrue() = runTest {
        // ARRANGE
        coEvery { api.enviarTarea(1, EnviarTareaSolicitud(2, "Ya regué", "file:/foto.jpg")) } returns dto(1)
        coEvery {
            dao.enviarARevision(1, 2, "file:/foto.jpg", "Ya regué", "PENDIENTE_DE_VALIDACION")
        } returns 1

        // ACT
        val ok = repositorio.marcarComoRealizada(1, 2, "file:/foto.jpg", "Ya regué")

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun marcarComoRealizada_cultivoDeOtroEstudianteEnElDao_retornaFalse() = runTest {
        // ARRANGE
        coEvery { api.enviarTarea(1, any()) } returns dto(1)
        coEvery {
            dao.enviarARevision(1, 99, "file:/foto.jpg", "Ya regué", "PENDIENTE_DE_VALIDACION")
        } returns 0

        // ACT
        val ok = repositorio.marcarComoRealizada(1, 99, "file:/foto.jpg", "Ya regué")

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun marcarComoRealizada_comentarioConEspacios_loEnviaYLoGuardaSinEspacios() = runTest {
        // ARRANGE
        coEvery {
            api.enviarTarea(1, EnviarTareaSolicitud(2, "listo el riego", "file:/foto.jpg"))
        } returns dto(1)
        coEvery {
            dao.enviarARevision(1, 2, "file:/foto.jpg", "listo el riego", "PENDIENTE_DE_VALIDACION")
        } returns 1

        // ACT
        val ok = repositorio.marcarComoRealizada(1, 2, "file:/foto.jpg", "  listo el riego  ")

        // ASSERT
        assertTrue(ok)
        coVerify(exactly = 1) {
            api.enviarTarea(1, EnviarTareaSolicitud(2, "listo el riego", "file:/foto.jpg"))
        }
    }

    @Test
    fun marcarComoRealizada_servidorRechaza_lanzaLaExcepcionYNoTocaElDao() {
        // ARRANGE
        coEvery { api.enviarTarea(any(), any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) {
            runBlocking { repositorio.marcarComoRealizada(1, 2, "file:/foto.jpg", "Ya regué") }
        }
        coVerify(exactly = 0) { dao.enviarARevision(any(), any(), any(), any(), any()) }
    }

    // ---------- tarea: validar y rechazar ----------

    @Test
    fun validar_tareaPendienteDeValidacion_retornaTrue() = runTest {
        // ARRANGE
        coEvery { api.validarTarea(1) } returns dto(1)
        coEvery { dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "VALIDADA", null) } returns 1

        // ACT
        val ok = repositorio.validar(1)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun validar_tareaQueNoEstaPendienteEnElDao_retornaFalse() = runTest {
        // ARRANGE
        coEvery { api.validarTarea(1) } returns dto(1)
        coEvery { dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "VALIDADA", null) } returns 0

        // ACT
        val ok = repositorio.validar(1)

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun validar_servidorRechaza_lanzaLaExcepcionYNoTocaElDao() {
        // ARRANGE
        coEvery { api.validarTarea(1) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.validar(1) } }
        coVerify(exactly = 0) { dao.resolverRevision(any(), any(), any(), any()) }
    }

    @Test
    fun rechazar_conMotivoConEspacios_loEnviaYLoGuardaSinEspacios() = runTest {
        // ARRANGE
        coEvery { api.rechazarTarea(1, RechazarTareaSolicitud("falta riego")) } returns dto(1)
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "PENDIENTE", "falta riego")
        } returns 1

        // ACT
        val ok = repositorio.rechazar(1, "  falta riego  ")

        // ASSERT
        assertTrue(ok)
        coVerify(exactly = 1) { api.rechazarTarea(1, RechazarTareaSolicitud("falta riego")) }
    }

    @Test
    fun rechazar_tareaQueNoEstaPendienteEnElDao_retornaFalse() = runTest {
        // ARRANGE
        coEvery { api.rechazarTarea(1, any()) } returns dto(1)
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "PENDIENTE", "foto borrosa")
        } returns 0

        // ACT
        val ok = repositorio.rechazar(1, "foto borrosa")

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun rechazar_servidorRechaza_lanzaLaExcepcionYNoTocaElDao() {
        // ARRANGE
        coEvery { api.rechazarTarea(any(), any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.rechazar(1, "foto borrosa") } }
        coVerify(exactly = 0) { dao.resolverRevision(any(), any(), any(), any()) }
    }

    // ---------- sincronizar ----------

    @Test
    fun sincronizar_servidorResponde_reemplazaLaCopiaLocal() = runTest {
        // ARRANGE
        coEvery { api.obtenerCultivos() } returns listOf(dto(1), dto(2))
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT
        coVerifyOrder {
            dao.eliminarTodos()
            dao.insertarTodos(listOf(cultivo(1), cultivo(2)))
        }
    }

    @Test
    fun sincronizar_servidorNoResponde_conservaLaCopiaLocal() {
        // ARRANGE
        coEvery { api.obtenerCultivos() } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.sincronizar() } }
        coVerify(exactly = 0) { dao.eliminarTodos() }
        coVerify(exactly = 0) { dao.insertarTodos(any()) }
    }
}
