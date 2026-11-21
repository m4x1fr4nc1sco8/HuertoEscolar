package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.remote.ClimaApi
import cl.duoc.huertoescolar.data.remote.fechaALocal
import cl.duoc.huertoescolar.domain.model.Clima
import cl.duoc.huertoescolar.domain.model.PronosticoDia

// Clima de San Bernardo desde Open-Meteo (API externa)
class ClimaRepository(
    private val api: ClimaApi
) {
    suspend fun obtenerClima(): Clima {
        val respuesta = api.obtenerClima(LATITUD, LONGITUD)
        val actual = respuesta.actual
        val diario = respuesta.diario
        val (icono, descripcion) = condicion(actual.codigo)

        val pronostico = diario.fechas.indices.map { i ->
            PronosticoDia(
                // "2026-11-22" -> "22/11"
                fecha = fechaALocal(diario.fechas[i]).take(5),
                icono = condicion(diario.codigos.getOrElse(i) { -1 }).first,
                minima = diario.minimas.getOrElse(i) { 0.0 },
                maxima = diario.maximas.getOrElse(i) { 0.0 },
                probabilidadLluvia = diario.probabilidadLluvia.getOrNull(i) ?: 0
            )
        }

        return Clima(
            temperatura = actual.temperatura,
            humedad = actual.humedad,
            vientoKmH = actual.viento,
            descripcion = descripcion,
            icono = icono,
            pronostico = pronostico
        )
    }

    // Códigos de clima WMO que usa Open-Meteo -> (ícono, descripción)
    private fun condicion(codigo: Int): Pair<String, String> = when (codigo) {
        0 -> "☀️" to "Despejado"
        1 -> "🌤️" to "Mayormente despejado"
        2 -> "⛅" to "Parcialmente nublado"
        3 -> "☁️" to "Nublado"
        45, 48 -> "🌫️" to "Niebla"
        in 51..57 -> "🌦️" to "Llovizna"
        in 61..67 -> "🌧️" to "Lluvia"
        in 71..77 -> "❄️" to "Nieve"
        in 80..82 -> "🌧️" to "Chubascos"
        85, 86 -> "❄️" to "Chubascos de nieve"
        in 95..99 -> "⛈️" to "Tormenta"
        else -> "🌡️" to "Sin información"
    }

    companion object {
        // San Bernardo, Región Metropolitana
        const val LATITUD = -33.5933
        const val LONGITUD = -70.6993
    }
}
