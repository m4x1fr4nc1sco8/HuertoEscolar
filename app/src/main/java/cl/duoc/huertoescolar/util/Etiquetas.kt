package cl.duoc.huertoescolar.util

import cl.duoc.huertoescolar.model.EstadoCultivo

fun EstadoCultivo.etiqueta(): String = when (this) {
    EstadoCultivo.PLANIFICADO -> "Planificado"
    EstadoCultivo.SEMBRADO -> "Sembrado"
    EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
    EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
    EstadoCultivo.COSECHADO -> "Cosechado"
    EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
    EstadoCultivo.FINALIZADO -> "Finalizado"
}