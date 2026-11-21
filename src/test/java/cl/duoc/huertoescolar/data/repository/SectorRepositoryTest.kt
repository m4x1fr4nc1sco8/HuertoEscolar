package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.SectorDao
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectorSolicitud
import cl.duoc.huertoescolar.domain.model.Sector
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class SectorRepositoryTest {

    private lateinit var dao: SectorDao
    private lateinit var api: HuertoApi
    private lateinit var repositorio: SectorRepository

    @Before
    fun preparar() {
        dao = mockk()
        api = mockk()
        repositorio = SectorRepository(dao, api)
    }

    private fun sector(id: Int = 1, codigo: String = "S-01") = Sector(
        id = id,
        codigo = codigo,
        nombre = "Tomates",
        ubicacion = "patio norte",
        estado = "DISPONIBLE",
        fechaCreacion = "17/11/2026"
    )

    private fun dto(id: Int = 1, codigo: String = "S-01") = SectorDto(
        id = id,
        codigo = codigo,
        nombre = "Tomates",
        ubicacion = "patio norte",
        estado = "DISPONIBLE",
        fechaCreacion = "2026-11-17"
    )

    // ---------- crear ----------

    @Test
    fun guardar_servidorCreaElSector_loEspejaEnElDaoConLaIdDelServidor() = runTest {
        // ARRANGE
        val solicitud = slot<SectorSolicitud>()
        coEvery { api.crearSector(capture(solicitud)) } returns dto(id = 7)
        coEvery { dao.insertar(any()) } just Runs

        // ACT
        repositorio.guardar(sector(id = 0))

        // ASSERT
        assertEquals(
            SectorSolicitud("S-01", "Tomates", "patio norte", "DISPONIBLE", null),
            solicitud.captured
        )
        coVerify(exactly = 1) { dao.insertar(sector(id = 7)) }
    }

    @Test
    fun guardar_servidorNoResponde_lanzaLaExcepcionYNoInsertaEnElDao() {
        // ARRANGE
        coEvery { api.crearSector(any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) {
            runBlocking { repositorio.guardar(sector(id = 0)) }
        }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    // ---------- leer (siempre desde Room) ----------

    @Test
    fun obtenerSectores_daoConSectores_emiteLaMismaLista() = runTest {
        // ARRANGE
        val lista = listOf(sector(1, "S-01"), sector(2, "S-02"))
        every { dao.obtenerSectores() } returns flowOf(lista)

        // ACT
        val resultado = repositorio.obtenerSectores().first()

        // ASSERT
        assertEquals(lista, resultado)
    }

    @Test
    fun existeCodigo_hayCoincidencias_retornaTrue() = runTest {
        coEvery { dao.contarPorCodigo("S-01") } returns 1

        assertTrue(repositorio.existeCodigo("S-01"))
    }

    @Test
    fun existeCodigo_sinCoincidencias_retornaFalse() = runTest {
        coEvery { dao.contarPorCodigo("S-99") } returns 0

        assertFalse(repositorio.existeCodigo("S-99"))
    }

    @Test
    fun obtenerPorId_sectorExistente_retornaElSector() = runTest {
        // ARRANGE
        val tomates = sector()
        coEvery { dao.obtenerPorId(1) } returns tomates

        // ACT
        val resultado = repositorio.obtenerPorId(1)

        // ASSERT
        assertEquals(tomates, resultado)
    }

    @Test
    fun obtenerPorId_sectorInexistente_retornaNull() = runTest {
        coEvery { dao.obtenerPorId(404) } returns null

        assertNull(repositorio.obtenerPorId(404))
    }

    @Test
    fun existeCodigoEnOtroSector_codigoUsadoPorOtro_retornaTrue() = runTest {
        coEvery { dao.contarPorCodigoExcluyendo("S-02", 1) } returns 1

        assertTrue(repositorio.existeCodigoEnOtroSector("S-02", 1))
    }

    @Test
    fun existeCodigoEnOtroSector_codigoSoloDelPropioSector_retornaFalse() = runTest {
        coEvery { dao.contarPorCodigoExcluyendo("S-01", 1) } returns 0

        assertFalse(repositorio.existeCodigoEnOtroSector("S-01", 1))
    }

    // ---------- actualizar ----------

    @Test
    fun actualizar_servidorYDaoActualizan_retornaTrue() = runTest {
        // ARRANGE
        coEvery {
            api.actualizarSector(1, SectorSolicitud("S-02", "Lechugas", "patio sur", "EN_USO", null))
        } returns dto(1, "S-02")
        coEvery { dao.actualizar(1, "S-02", "Lechugas", "patio sur", "EN_USO", null) } returns 1

        // ACT
        val ok = repositorio.actualizar(1, "S-02", "Lechugas", "patio sur", "EN_USO", null)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun actualizar_sectorInexistenteEnElDao_retornaFalse() = runTest {
        // ARRANGE
        coEvery { api.actualizarSector(404, any()) } returns dto(404)
        coEvery {
            dao.actualizar(404, "S-02", "Lechugas", "patio sur", "EN_USO", "file:/foto.jpg")
        } returns 0

        // ACT
        val ok = repositorio.actualizar(404, "S-02", "Lechugas", "patio sur", "EN_USO", "file:/foto.jpg")

        // ASSERT
        assertFalse(ok)
    }

    @Test
    fun actualizar_servidorRechaza_lanzaLaExcepcionYNoTocaElDao() {
        // ARRANGE
        coEvery { api.actualizarSector(any(), any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) {
            runBlocking { repositorio.actualizar(1, "S-02", "Lechugas", "patio sur", "EN_USO", null) }
        }
        coVerify(exactly = 0) { dao.actualizar(any(), any(), any(), any(), any(), any()) }
    }

    // ---------- eliminar ----------

    @Test
    fun eliminar_servidorElimina_tambienLoBorraDelDao() = runTest {
        // ARRANGE
        coEvery { api.eliminarSector(3) } just Runs
        coEvery { dao.eliminar(3) } just Runs

        // ACT
        repositorio.eliminar(3)

        // ASSERT
        coVerifyOrder {
            api.eliminarSector(3)
            dao.eliminar(3)
        }
    }

    @Test
    fun eliminar_servidorRechaza_noLoBorraDelDao() {
        // ARRANGE
        coEvery { api.eliminarSector(3) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.eliminar(3) } }
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }

    // ---------- sincronizar ----------

    @Test
    fun sincronizar_servidorResponde_reemplazaLaCopiaLocal() = runTest {
        // ARRANGE
        coEvery { api.obtenerSectores() } returns listOf(dto(1, "S-01"), dto(2, "S-02"))
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT
        coVerifyOrder {
            dao.eliminarTodos()
            dao.insertarTodos(listOf(sector(1, "S-01"), sector(2, "S-02")))
        }
    }

    @Test
    fun sincronizar_servidorNoResponde_conservaLaCopiaLocal() {
        // ARRANGE
        coEvery { api.obtenerSectores() } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.sincronizar() } }
        coVerify(exactly = 0) { dao.eliminarTodos() }
        coVerify(exactly = 0) { dao.insertarTodos(any()) }
    }
}
