package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.SectorDao
import cl.duoc.huertoescolar.domain.model.Sector
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SectorRepositoryTest {

    private lateinit var dao: SectorDao
    private lateinit var repositorio: SectorRepository

    @Before
    fun preparar() {
        dao = mockk()
        repositorio = SectorRepository(dao)
    }

    private fun sector(id: Int = 1, codigo: String = "S-01") = Sector(
        id = id,
        codigo = codigo,
        nombre = "Tomates",
        ubicacion = "patio norte",
        estado = "DISPONIBLE",
        fechaCreacion = "17/11/2026"
    )

    @Test
    fun guardar_sectorValido_loInsertaEnElDao() = runTest {
        // ARRANGE
        val tomates = sector()
        coEvery { dao.insertar(tomates) } just Runs

        // ACT
        repositorio.guardar(tomates)

        // ASSERT
        coVerify(exactly = 1) { dao.insertar(tomates) }
    }

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
        // ARRANGE
        coEvery { dao.contarPorCodigo("S-01") } returns 1

        // ACT
        val existe = repositorio.existeCodigo("S-01")

        // ASSERT
        assertTrue(existe)
    }

    @Test
    fun existeCodigo_sinCoincidencias_retornaFalse() = runTest {
        // ARRANGE
        coEvery { dao.contarPorCodigo("S-99") } returns 0

        // ACT
        val existe = repositorio.existeCodigo("S-99")

        // ASSERT
        assertFalse(existe)
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
        // ARRANGE
        coEvery { dao.obtenerPorId(404) } returns null

        // ACT
        val resultado = repositorio.obtenerPorId(404)

        // ASSERT
        assertNull(resultado)
    }

    @Test
    fun existeCodigoEnOtroSector_codigoUsadoPorOtro_retornaTrue() = runTest {
        // ARRANGE
        coEvery { dao.contarPorCodigoExcluyendo("S-02", 1) } returns 1

        // ACT
        val existe = repositorio.existeCodigoEnOtroSector("S-02", 1)

        // ASSERT
        assertTrue(existe)
    }

    @Test
    fun existeCodigoEnOtroSector_codigoSoloDelPropioSector_retornaFalse() = runTest {
        // ARRANGE
        coEvery { dao.contarPorCodigoExcluyendo("S-01", 1) } returns 0

        // ACT
        val existe = repositorio.existeCodigoEnOtroSector("S-01", 1)

        // ASSERT
        assertFalse(existe)
    }

    @Test
    fun actualizar_sectorExistente_retornaTrue() = runTest {
        // ARRANGE
        coEvery { dao.actualizar(1, "S-02", "Lechugas", "patio sur", "EN_USO", null) } returns 1

        // ACT
        val ok = repositorio.actualizar(1, "S-02", "Lechugas", "patio sur", "EN_USO", null)

        // ASSERT
        assertTrue(ok)
    }

    @Test
    fun actualizar_sectorInexistente_retornaFalse() = runTest {
        // ARRANGE
        coEvery { dao.actualizar(404, "S-02", "Lechugas", "patio sur", "EN_USO", "file:/foto.jpg") } returns 0

        // ACT
        val ok = repositorio.actualizar(404, "S-02", "Lechugas", "patio sur", "EN_USO", "file:/foto.jpg")

        // ASSERT
        assertFalse(ok)
    }
}
