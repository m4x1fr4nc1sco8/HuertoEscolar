package cl.duoc.huertoescolar.domain.alertas

import cl.duoc.huertoescolar.domain.model.Alerta
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.TipoAlerta
import cl.duoc.huertoescolar.util.fechaDeNumeroDeDia
import cl.duoc.huertoescolar.util.numeroDeDia
import cl.duoc.huertoescolar.util.numeroDeDiaDeTexto
import java.util.Date

// HU19: calcula qué riegos y cosechas hay que atender, a partir de los datos de cada cultivo.
// No guarda nada: las alertas se calculan cada vez, así que nunca quedan desactualizadas.
object GeneradorAlertas {

    // Se avisa del riego de hoy y de los que vienen en los próximos días
    const val DIAS_AVISO_RIEGO = 2

    // Se avisa de las cosechas que ocurren dentro de esta cantidad de días
    const val DIAS_AVISO_COSECHA = 7

    fun generar(cultivos: List<Cultivo>, hoy: Date = Date()): List<Alerta> {
        val diaHoy = numeroDeDia(hoy)
        return cultivos
            .flatMap { alertasDe(it, diaHoy) }
            .sortedWith(compareBy({ it.tipo.ordinal }, { it.dias }))
    }

    private fun alertasDe(cultivo: Cultivo, diaHoy: Long): List<Alerta> {
        // Un cultivo cosechado o finalizado ya no necesita riego ni cosecha
        if (cultivo.estado == EstadoCultivo.COSECHADO.name ||
            cultivo.estado == EstadoCultivo.FINALIZADO.name
        ) return emptyList()

        val diaSiembra = numeroDeDiaDeTexto(cultivo.fechaSiembra)
        val diaCosecha = numeroDeDiaDeTexto(cultivo.fechaCosecha)
        val alertas = mutableListOf<Alerta>()

        if (diaCosecha != null) {
            val faltan = diaCosecha - diaHoy
            val tipo = when {
                faltan < 0 -> TipoAlerta.COSECHA_VENCIDA
                faltan == 0L -> TipoAlerta.COSECHA_HOY
                faltan <= DIAS_AVISO_COSECHA -> TipoAlerta.COSECHA_PROXIMA
                else -> null
            }
            if (tipo != null) {
                alertas += Alerta(
                    cultivoId = cultivo.id,
                    cultivoNombre = cultivo.nombre,
                    tipo = tipo,
                    dias = Math.abs(faltan).toInt(),
                    fecha = fechaDeNumeroDeDia(diaCosecha)
                )
            }
        }

        // Riego: desde el día de siembra, cada N días, hasta la fecha de cosecha
        val frecuencia = cultivo.frecuenciaRiegoDias.toLong()
        if (diaSiembra != null && diaCosecha != null && frecuencia > 0 && diaHoy <= diaCosecha) {
            val proximoRiego = proximoRiego(diaSiembra, frecuencia, diaHoy)
            val faltan = proximoRiego - diaHoy
            if (proximoRiego <= diaCosecha && faltan <= DIAS_AVISO_RIEGO) {
                alertas += Alerta(
                    cultivoId = cultivo.id,
                    cultivoNombre = cultivo.nombre,
                    tipo = if (faltan == 0L) TipoAlerta.RIEGO_HOY else TipoAlerta.RIEGO_PROXIMO,
                    dias = faltan.toInt(),
                    fecha = fechaDeNumeroDeDia(proximoRiego)
                )
            }
        }
        return alertas
    }

    // Primer día de riego que cae hoy o después. Los riegos son: siembra, siembra + N, siembra + 2N...
    private fun proximoRiego(diaSiembra: Long, frecuencia: Long, diaHoy: Long): Long {
        if (diaHoy <= diaSiembra) return diaSiembra
        val periodosTranscurridos = (diaHoy - diaSiembra + frecuencia - 1) / frecuencia
        return diaSiembra + periodosTranscurridos * frecuencia
    }
}
