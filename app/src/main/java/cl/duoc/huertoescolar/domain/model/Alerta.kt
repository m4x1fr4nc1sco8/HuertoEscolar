package cl.duoc.huertoescolar.domain.model

// El orden de los valores es el orden de urgencia con el que se muestran las alertas
enum class TipoAlerta {
    COSECHA_VENCIDA,
    RIEGO_HOY,
    COSECHA_HOY,
    RIEGO_PROXIMO,
    COSECHA_PROXIMA
}

data class Alerta(
    val cultivoId: Int,
    val cultivoNombre: String,
    val tipo: TipoAlerta,
    // Días que faltan (próximas) o que lleva de atraso (cosecha vencida). 0 = hoy.
    val dias: Int,
    // Fecha del riego o de la cosecha (dd/MM/yyyy)
    val fecha: String
)
