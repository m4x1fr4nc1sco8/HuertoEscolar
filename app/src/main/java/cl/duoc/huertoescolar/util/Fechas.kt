package cl.duoc.huertoescolar.util

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FORMATO_FECHA = "dd/MM/yyyy"

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
