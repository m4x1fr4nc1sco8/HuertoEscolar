package cl.duoc.huertoescolar.data.util

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.util.estaAtrasada
import cl.duoc.huertoescolar.util.fechaYaPaso
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FechasTest {

    private val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // "Hoy" fijo: 18/11/2026 a las 15:30, para que los tests no dependan del reloj
    private val hoy: Date =
        Date(formato.parse("18/11/2026")!!.time + (15 * 60 + 30) * 60 * 1000L)

    private fun cultivo(estadoTarea: EstadoTarea, fechaLimite: String?) = Cultivo(
        id = 1,
        nombre = "Tomate",
        sectorId = 1,
        fechaSiembra = "01/03/2026",
        fechaCosecha = "01/06/2027",
        frecuenciaRiegoDias = 3,
        estado = EstadoCultivo.SEMBRADO.name,
        estudianteId = 2,
        estadoTarea = estadoTarea.name,
        fechaLimite = fechaLimite
    )

    // ---------- fechaYaPaso ----------

    @Test
    fun fechaYaPaso_fechaAnteriorAHoy_retornaTrue() {
        assertTrue(fechaYaPaso("17/11/2026", hoy))
    }

    @Test
    fun fechaYaPaso_fechaDeHoy_retornaFalse() {
        // El día de la fecha límite todavía no está atrasada, aunque ya haya pasado la hora 00:00
        assertFalse(fechaYaPaso("18/11/2026", hoy))
    }

    @Test
    fun fechaYaPaso_fechaFutura_retornaFalse() {
        assertFalse(fechaYaPaso("19/11/2026", hoy))
    }

    @Test
    fun fechaYaPaso_textoNuloOVacio_retornaFalse() {
        assertFalse(fechaYaPaso(null, hoy))
        assertFalse(fechaYaPaso("   ", hoy))
    }

    @Test
    fun fechaYaPaso_textoConFormatoInvalido_retornaFalse() {
        assertFalse(fechaYaPaso("ayer", hoy))
        assertFalse(fechaYaPaso("31/02/2026", hoy))
    }

    // ---------- Cultivo.estaAtrasada ----------

    @Test
    fun estaAtrasada_tareaPendienteConFechaPasada_retornaTrue() {
        assertTrue(cultivo(EstadoTarea.PENDIENTE, "10/11/2026").estaAtrasada(hoy))
    }

    @Test
    fun estaAtrasada_tareaPendienteConFechaFutura_retornaFalse() {
        assertFalse(cultivo(EstadoTarea.PENDIENTE, "30/11/2026").estaAtrasada(hoy))
    }

    @Test
    fun estaAtrasada_tareaYaEnviadaAunqueLaFechaPaso_retornaFalse() {
        assertFalse(cultivo(EstadoTarea.PENDIENTE_DE_VALIDACION, "10/11/2026").estaAtrasada(hoy))
        assertFalse(cultivo(EstadoTarea.VALIDADA, "10/11/2026").estaAtrasada(hoy))
    }

    @Test
    fun estaAtrasada_tareaSinFechaLimite_retornaFalse() {
        assertFalse(cultivo(EstadoTarea.PENDIENTE, null).estaAtrasada(hoy))
    }
}
