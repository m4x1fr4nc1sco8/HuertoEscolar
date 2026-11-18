package cl.duoc.huertoescolar.domain.alertas

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.TipoAlerta
import cl.duoc.huertoescolar.util.numeroDeDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeneradorAlertasTest {

    private val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // "Hoy" fijo: 18/11/2026 a las 15:30, para que los tests no dependan del reloj
    private val hoy: Date =
        Date(formato.parse("18/11/2026")!!.time + (15 * 60 + 30) * 60 * 1000L)

    private fun cultivo(
        id: Int = 1,
        nombre: String = "Tomate",
        siembra: String = "10/11/2026",
        cosecha: String = "30/12/2026",
        riegoCadaDias: Int = 2,
        estado: EstadoCultivo = EstadoCultivo.EN_CRECIMIENTO
    ) = Cultivo(
        id = id,
        nombre = nombre,
        sectorId = 1,
        fechaSiembra = siembra,
        fechaCosecha = cosecha,
        frecuenciaRiegoDias = riegoCadaDias,
        estado = estado.name
    )

    // ---------- Riego ----------

    @Test
    fun generar_riegoQueCaeHoy_creaAlertaRiegoHoy() {
        // ARRANGE: riegos el 10, 12, 14, 16 y 18 de noviembre
        val cultivos = listOf(cultivo(siembra = "10/11/2026", riegoCadaDias = 2))

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.RIEGO_HOY, alertas[0].tipo)
        assertEquals(0, alertas[0].dias)
        assertEquals("18/11/2026", alertas[0].fecha)
    }

    @Test
    fun generar_riegoMananaDentroDelAviso_creaAlertaRiegoProximo() {
        // ARRANGE: riegos el 10, 13, 16 y 19 de noviembre
        val cultivos = listOf(cultivo(siembra = "10/11/2026", riegoCadaDias = 3))

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.RIEGO_PROXIMO, alertas[0].tipo)
        assertEquals(1, alertas[0].dias)
        assertEquals("19/11/2026", alertas[0].fecha)
    }

    @Test
    fun generar_riegoLejanoFueraDelAviso_noCreaAlerta() {
        // ARRANGE: riegos el 10, 17 y 24 de noviembre; el próximo es en 6 días
        val cultivos = listOf(cultivo(siembra = "10/11/2026", riegoCadaDias = 7))

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertTrue(alertas.isEmpty())
    }

    @Test
    fun generar_cultivoQueAunNoSeSiembraConSiembraManana_avisaElPrimerRiego() {
        // ARRANGE
        val cultivos = listOf(cultivo(siembra = "19/11/2026", riegoCadaDias = 5))

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.RIEGO_PROXIMO, alertas[0].tipo)
        assertEquals("19/11/2026", alertas[0].fecha)
    }

    @Test
    fun generar_proximoRiegoDespuesDeLaCosecha_noCreaAlertaDeRiego() {
        // ARRANGE: riegos el 10 y el 20, pero la cosecha es el 19
        val cultivos = listOf(
            cultivo(siembra = "10/11/2026", cosecha = "19/11/2026", riegoCadaDias = 10)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT: solo la alerta de cosecha
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.COSECHA_PROXIMA, alertas[0].tipo)
    }

    // ---------- Cosecha ----------

    @Test
    fun generar_cosechaEnCuatroDias_creaAlertaCosechaProxima() {
        // ARRANGE: el riego del 21 queda a 3 días, fuera del aviso
        val cultivos = listOf(
            cultivo(siembra = "01/11/2026", cosecha = "22/11/2026", riegoCadaDias = 20)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.COSECHA_PROXIMA, alertas[0].tipo)
        assertEquals(4, alertas[0].dias)
        assertEquals("22/11/2026", alertas[0].fecha)
    }

    @Test
    fun generar_cosechaHoy_creaAlertaCosechaHoy() {
        // ARRANGE
        val cultivos = listOf(
            cultivo(siembra = "01/11/2026", cosecha = "18/11/2026", riegoCadaDias = 30)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertTrue(alertas.any { it.tipo == TipoAlerta.COSECHA_HOY && it.dias == 0 })
    }

    @Test
    fun generar_cosechaYaVencidaSinCosechar_creaAlertaCosechaVencida() {
        // ARRANGE
        val cultivos = listOf(
            cultivo(siembra = "01/10/2026", cosecha = "15/11/2026", riegoCadaDias = 3)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT: 3 días de atraso y ninguna alerta de riego (ya pasó la cosecha)
        assertEquals(1, alertas.size)
        assertEquals(TipoAlerta.COSECHA_VENCIDA, alertas[0].tipo)
        assertEquals(3, alertas[0].dias)
    }

    @Test
    fun generar_cosechaLejana_noCreaAlertaDeCosecha() {
        // ARRANGE: cosecha en más de 7 días y riego lejano
        val cultivos = listOf(
            cultivo(siembra = "10/11/2026", cosecha = "30/12/2026", riegoCadaDias = 7)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertTrue(alertas.isEmpty())
    }

    // ---------- Reglas generales ----------

    @Test
    fun generar_cultivoCosechadoOFinalizado_noCreaAlertas() {
        // ARRANGE: ambos tendrían alertas si siguieran activos
        val cultivos = listOf(
            cultivo(id = 1, cosecha = "15/11/2026", estado = EstadoCultivo.COSECHADO),
            cultivo(id = 2, siembra = "10/11/2026", riegoCadaDias = 2, estado = EstadoCultivo.FINALIZADO)
        )

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertTrue(alertas.isEmpty())
    }

    @Test
    fun generar_fechasInvalidas_noFallaYNoCreaAlertas() {
        // ARRANGE
        val cultivos = listOf(cultivo(siembra = "ayer", cosecha = "31/02/2026"))

        // ACT
        val alertas = GeneradorAlertas.generar(cultivos, hoy)

        // ASSERT
        assertTrue(alertas.isEmpty())
    }

    @Test
    fun generar_variasAlertas_lasOrdenaPorUrgencia() {
        // ARRANGE
        val cultivos = listOf(
            cultivo(id = 1, nombre = "Zapallo", siembra = "10/11/2026", cosecha = "23/11/2026", riegoCadaDias = 3),
            cultivo(id = 2, nombre = "Lechuga", siembra = "10/11/2026", riegoCadaDias = 2),
            cultivo(id = 3, nombre = "Acelga", siembra = "01/10/2026", cosecha = "15/11/2026", riegoCadaDias = 3)
        )

        // ACT
        val tipos = GeneradorAlertas.generar(cultivos, hoy).map { it.tipo }

        // ASSERT: primero lo vencido, luego lo de hoy, luego lo que viene
        assertEquals(
            listOf(
                TipoAlerta.COSECHA_VENCIDA,
                TipoAlerta.RIEGO_HOY,
                TipoAlerta.RIEGO_PROXIMO,
                TipoAlerta.COSECHA_PROXIMA
            ),
            tipos
        )
    }

    // ---------- numeroDeDia ----------

    @Test
    fun numeroDeDia_ignoraLaHoraDelDia() {
        // ARRANGE
        val madrugada = formato.parse("18/11/2026")!!
        val noche = Date(madrugada.time + (23 * 60 + 59) * 60 * 1000L)

        // ACT & ASSERT
        assertEquals(numeroDeDia(madrugada), numeroDeDia(noche))
    }

    @Test
    fun numeroDeDia_diasConsecutivosDifierenEnUno() {
        // ARRANGE
        val hoyDia = numeroDeDia(formato.parse("18/11/2026")!!)
        val mananaDia = numeroDeDia(formato.parse("19/11/2026")!!)

        // ACT & ASSERT
        assertEquals(1L, mananaDia - hoyDia)
    }
}
