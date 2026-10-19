package cl.duoc.huertoescolar.util

import cl.duoc.huertoescolar.model.EstadoCultivo
import cl.duoc.huertoescolar.model.EstadoTarea

fun EstadoCultivo.etiqueta(): String = when (this) {
    EstadoCultivo.PLANIFICADO -> "Planificado"
    EstadoCultivo.SEMBRADO -> "Sembrado"
    EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
    EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
    EstadoCultivo.COSECHADO -> "Cosechado"
    EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
    EstadoCultivo.FINALIZADO -> "Finalizado"
}

fun EstadoTarea.etiqueta(): String = when (this) {
    EstadoTarea.PENDIENTE -> "Pendiente"
    EstadoTarea.EN_DESARROLLO -> "En desarrollo"
    EstadoTarea.REALIZADA -> "Realizada"
    EstadoTarea.PENDIENTE_DE_VALIDACION -> "Pendiente de validación"
    EstadoTarea.VALIDADA -> "Validada"
    EstadoTarea.RECHAZADA -> "Rechazada"
    EstadoTarea.ATRASADA -> "Atrasada"
}
