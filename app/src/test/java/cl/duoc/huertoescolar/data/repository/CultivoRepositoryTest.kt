package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.CultivoDao
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CultivoRepositoryTest {

    private lateinit var dao: CultivoDao
    private lateinit var repositorio: CultivoRepository

    @Before
    fun preparar() {
        dao = mockk()
        repositorio = CultivoRepository(dao)
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

    @Test
    fun guardar_cultivoValido_loInsertaEnElDao() = runTest {
        // ARRANGE
        val tomate = cultivo()
        coEvery { dao.insertar(tomate) } just Runs

        // ACT
        repositorio.guardar(tomate)

        // ASSERT
        coVerify(exactly = 1) { dao.insertar(tomate) }
    }

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
    fun cambiarEstado_cultivoExistente_retornaTrue() = runTest {
        // ARRANGE
        coEvery { dao.actualizarEstado(1, "COSECHADO") } returns 1

        // ACT
        val ok = repositorio.cambiarEstado(1, EstadoCultivo.COSECHADO)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun cambiarEstado_cultivoInexistente_retornaFalse() = runTest {
        // ARRANGE
        coEvery { dao.actualizarEstado(404, "COSECHADO") } returns 0

        // ACT
        val ok = repositorio.cambiarEstado(404, EstadoCultivo.COSECHADO)

        // ASSERT
        assertFalse(ok)
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

    @Test
    fun marcarComoRealizada_cultivoDelEstudiante_retornaTrue() = runTest {
        // ARRANGE
        coEvery {
            dao.enviarARevision(1, 2, "file:/foto.jpg", "PENDIENTE_DE_VALIDACION")
        } returns 1

        // ACT
        val ok = repositorio.marcarComoRealizada(1, 2, "file:/foto.jpg")

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun marcarComoRealizada_cultivoDeOtroEstudiante_retornaFalse() = runTest {
        // ARRANGE
        coEvery {
            dao.enviarARevision(1, 99, "file:/foto.jpg", "PENDIENTE_DE_VALIDACION")
        } returns 0

        // ACT
        val ok = repositorio.marcarComoRealizada(1, 99, "file:/foto.jpg")

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun validar_tareaPendienteDeValidacion_retornaTrue() = runTest {
        // ARRANGE
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "VALIDADA", null)
        } returns 1

        // ACT
        val ok = repositorio.validar(1)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun validar_tareaQueNoEstaPendienteDeValidacion_retornaFalse() = runTest {
        // ARRANGE
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "VALIDADA", null)
        } returns 0

        // ACT
        val ok = repositorio.validar(1)

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun rechazar_conMotivoConEspacios_guardaElMotivoSinEspacios() = runTest {
        // ARRANGE
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "PENDIENTE", "falta riego")
        } returns 1

        // ACT
        val ok = repositorio.rechazar(1, "  falta riego  ")

        // ASSERT
        assertTrue(ok)
        coVerify(exactly = 1) {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "PENDIENTE", "falta riego")
        }
    }

    @Test
    fun rechazar_tareaQueNoEstaPendienteDeValidacion_retornaFalse() = runTest {
        // ARRANGE
        coEvery {
            dao.resolverRevision(1, "PENDIENTE_DE_VALIDACION", "PENDIENTE", "foto borrosa")
        } returns 0

        // ACT
        val ok = repositorio.rechazar(1, "foto borrosa")

        // ASSERT
        assertFalse(ok)
    }
}
