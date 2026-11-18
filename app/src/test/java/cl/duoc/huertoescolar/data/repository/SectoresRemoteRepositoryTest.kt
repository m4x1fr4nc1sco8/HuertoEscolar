package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectoresApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class SectoresRemoteRepositoryTest {

    private val api = mockk<SectoresApi>()
    private val repositorio = SectoresRemoteRepository(api)

    @Test
    fun obtenerSectores_apiRespondeConSectores_retornaLaMismaLista() = runTest {
        // ARRANGE
        val lista = listOf(
            SectorDto(1, "S-01", "Tomates", "patio norte", "DISPONIBLE", "17/11/2026"),
            SectorDto(2, "S-02", "Lechugas", "patio sur", "EN_USO", "18/11/2026")
        )
        coEvery { api.obtenerSectores() } returns lista

        // ACT
        val resultado = repositorio.obtenerSectores()

        // ASSERT
        assertEquals(lista, resultado)
    }

    @Test
    fun obtenerSectores_apiRespondeListaVacia_retornaListaVacia() = runTest {
        // ARRANGE
        coEvery { api.obtenerSectores() } returns emptyList()

        // ACT
        val resultado = repositorio.obtenerSectores()

        // ASSERT
        assertEquals(emptyList<SectorDto>(), resultado)
    }

    @Test
    fun obtenerSectores_apiFallaPorRed_propagaLaExcepcion() {
        // ARRANGE
        coEvery { api.obtenerSectores() } throws IOException("sin conexión")

        // ACT + ASSERT
        assertThrows(IOException::class.java) {
            runBlocking { repositorio.obtenerSectores() }
        }
    }
}
