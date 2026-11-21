package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.remote.ClimaActualDto
import cl.duoc.huertoescolar.data.remote.ClimaApi
import cl.duoc.huertoescolar.data.remote.ClimaDiarioDto
import cl.duoc.huertoescolar.data.remote.ClimaRespuesta
import cl.duoc.huertoescolar.domain.model.PronosticoDia
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ClimaRepositoryTest {

    private lateinit var api: ClimaApi
    private lateinit var repositorio: ClimaRepository

    @Before
    fun preparar() {
        api = mockk()
        repositorio = ClimaRepository(api)
    }

    private fun respuesta(codigoActual: Int = 1) = ClimaRespuesta(
        actual = ClimaActualDto(temperatura = 18.4, humedad = 62, viento = 9.6, codigo = codigoActual),
        diario = ClimaDiarioDto(
            fechas = listOf("2026-11-21", "2026-11-22", "2026-11-23"),
            codigos = listOf(0, 3, 61),
            maximas = listOf(24.0, 22.5, 19.0),
            minimas = listOf(9.0, 10.5, 11.0),
            probabilidadLluvia = listOf(0, 10, 80)
        )
    )

    @Test
    fun obtenerClima_consultaLasCoordenadasDeSanBernardo() = runTest {
        // ARRANGE
        coEvery { api.obtenerClima(any(), any()) } returns respuesta()

        // ACT
        repositorio.obtenerClima()

        // ASSERT
        coVerify(exactly = 1) {
            api.obtenerClima(ClimaRepository.LATITUD, ClimaRepository.LONGITUD)
        }
    }

    @Test
    fun obtenerClima_respuestaCorrecta_conviertePasaElClimaActual() = runTest {
        // ARRANGE
        coEvery { api.obtenerClima(any(), any()) } returns respuesta(codigoActual = 1)

        // ACT
        val clima = repositorio.obtenerClima()

        // ASSERT
        assertEquals(18.4, clima.temperatura, 0.001)
        assertEquals(62, clima.humedad)
        assertEquals(9.6, clima.vientoKmH, 0.001)
        assertEquals("Mayormente despejado", clima.descripcion)
        assertEquals("🌤️", clima.icono)
    }

    @Test
    fun obtenerClima_respuestaCorrecta_armaElPronosticoDeTresDias() = runTest {
        // ARRANGE
        coEvery { api.obtenerClima(any(), any()) } returns respuesta()

        // ACT
        val pronostico = repositorio.obtenerClima().pronostico

        // ASSERT
        assertEquals(
            listOf(
                PronosticoDia("21/11", "☀️", 9.0, 24.0, 0),
                PronosticoDia("22/11", "☁️", 10.5, 22.5, 10),
                PronosticoDia("23/11", "🌧️", 11.0, 19.0, 80)
            ),
            pronostico
        )
    }

    @Test
    fun obtenerClima_segunElCodigoWmo_retornaLaDescripcionCorrecta() = runTest {
        // ARRANGE
        val esperados = mapOf(
            0 to "Despejado",
            1 to "Mayormente despejado",
            2 to "Parcialmente nublado",
            3 to "Nublado",
            45 to "Niebla",
            48 to "Niebla",
            53 to "Llovizna",
            63 to "Lluvia",
            73 to "Nieve",
            81 to "Chubascos",
            85 to "Chubascos de nieve",
            95 to "Tormenta",
            999 to "Sin información"
        )

        for ((codigo, descripcion) in esperados) {
            coEvery { api.obtenerClima(any(), any()) } returns respuesta(codigoActual = codigo)

            // ACT
            val clima = repositorio.obtenerClima()

            // ASSERT
            assertEquals("código $codigo", descripcion, clima.descripcion)
        }
    }

    @Test
    fun obtenerClima_probabilidadDeLluviaNula_laMuestraComoCero() = runTest {
        // ARRANGE
        val base = respuesta()
        coEvery { api.obtenerClima(any(), any()) } returns base.copy(
            diario = base.diario.copy(probabilidadLluvia = listOf(null, null, null))
        )

        // ACT
        val pronostico = repositorio.obtenerClima().pronostico

        // ASSERT
        assertEquals(listOf(0, 0, 0), pronostico.map { it.probabilidadLluvia })
    }

    @Test
    fun obtenerClima_listasIncompletas_completaLosDatosFaltantes() = runTest {
        // ARRANGE: llegan 2 fechas pero solo 1 dato de cada tipo
        coEvery { api.obtenerClima(any(), any()) } returns ClimaRespuesta(
            actual = ClimaActualDto(15.0, 70, 5.0, 3),
            diario = ClimaDiarioDto(
                fechas = listOf("2026-11-21", "2026-11-22"),
                codigos = listOf(0),
                maximas = listOf(20.0),
                minimas = listOf(8.0),
                probabilidadLluvia = listOf(5)
            )
        )

        // ACT
        val pronostico = repositorio.obtenerClima().pronostico

        // ASSERT
        assertEquals(PronosticoDia("22/11", "🌡️", 0.0, 0.0, 0), pronostico[1])
    }

    @Test
    fun obtenerClima_sinConexion_lanzaLaExcepcion() {
        // ARRANGE
        coEvery { api.obtenerClima(any(), any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.obtenerClima() } }
    }
}
