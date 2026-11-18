package cl.duoc.huertoescolar.util

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val FORMATO_FECHA = "dd/MM/yyyy"
private const val MILIS_POR_DIA = 86_400_000L

// Fecha de hoy con el formato que usa la app (dd/MM/yyyy)
fun fechaActual(): String =
    SimpleDateFormat(FORMATO_FECHA, Locale.getDefault()).format(Date())

// true si la fecha (dd/MM/yyyy) es anterior a hoy. Se compara solo el día, sin la hora.
// Si el texto es nulo, vacío o no es una fecha válida, devuelve false.
fun fechaYaPaso(texto: String?, hoy: Date = Date()): Boolean {
    if (texto.isNullOrBlank()) return false
    val formato = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault()).apply { isLenient = false }
    val limite = try {
        formato.parse(texto.trim())
    } catch (e: ParseException) {
        null
    } ?: return false
    val inicioDeHoy = formato.parse(formato.format(hoy)) ?: return false
    return limite.before(inicioDeHoy)
}

// HU18: una tarea está atrasada si sigue pendiente (aún no se envía) y su fecha límite ya pasó.
fun Cultivo.estaAtrasada(hoy: Date = Date()): Boolean =
    estadoTarea == EstadoTarea.PENDIENTE.name && fechaYaPaso(fechaLimite, hoy)

// Número de día (días desde 1970) de una fecha. Sirve para restar fechas contando días
// completos, sin que la hora ni el cambio de horario afecten el resultado.
fun numeroDeDia(fecha: Date): Long {
    val local = Calendar.getInstance().apply { time = fecha }
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }
    return utc.timeInMillis / MILIS_POR_DIA
}

// Igual que numeroDeDia pero desde un texto dd/MM/yyyy. Devuelve null si no es una fecha válida.
fun numeroDeDiaDeTexto(texto: String?): Long? {
    if (texto.isNullOrBlank()) return null
    val formato = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault()).apply { isLenient = false }
    val fecha = try {
        formato.parse(texto.trim())
    } catch (e: ParseException) {
        null
    } ?: return null
    return numeroDeDia(fecha)
}

// Operación inversa: del número de día al texto dd/MM/yyyy
fun fechaDeNumeroDeDia(dia: Long): String {
    val formato = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(dia * MILIS_POR_DIA))
}
