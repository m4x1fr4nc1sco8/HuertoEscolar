package cl.duoc.huertoescolar.domain.model

data class Clima(
    val temperatura: Double,
    val humedad: Int,
    val vientoKmH: Double,
    val descripcion: String,
    val icono: String,
    val pronostico: List<PronosticoDia>
)

data class PronosticoDia(
    val fecha: String,
    val icono: String,
    val minima: Double,
    val maxima: Double,
    val probabilidadLluvia: Int
)
