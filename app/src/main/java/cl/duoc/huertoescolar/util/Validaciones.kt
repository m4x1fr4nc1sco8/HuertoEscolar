package cl.duoc.huertoescolar.util

data class ResultadoValidacion(
    val esValido: Boolean,
    val mensaje: String? = null
) {
    companion object {
        val OK = ResultadoValidacion(true)
        fun error(mensaje: String) = ResultadoValidacion(false, mensaje)
    }
}

object Validaciones {
    private val REGEX_CORREO =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarObligatorio(valor: String, campo: String): ResultadoValidacion =
        if (valor.isBlank()) ResultadoValidacion.error("$campo es obligatorio")
        else ResultadoValidacion.OK

    fun validarCorreo(correo: String): ResultadoValidacion = when {
        correo.isBlank() -> ResultadoValidacion.error("El correo es obligatorio")
        !REGEX_CORREO.matches(correo.trim()) ->
            ResultadoValidacion.error("Ingresa un correo válido")
        else -> ResultadoValidacion.OK
    }

    fun validarClave(clave: String): ResultadoValidacion = when {
        clave.isBlank() -> ResultadoValidacion.error("La contraseña es obligatoria")
        clave.length < 6 -> ResultadoValidacion.error("Mínimo 6 caracteres")
        else -> ResultadoValidacion.OK
    }

    fun validarNombre(nombre: String, campo: String = "El nombre"): ResultadoValidacion = when {
        nombre.isBlank() -> ResultadoValidacion.error("$campo es obligatorio")
        nombre.trim().length < 3 -> ResultadoValidacion.error("$campo debe tener al menos 3 caracteres")
        else -> ResultadoValidacion.OK
    }

    fun validarFrecuenciaRiego(texto: String): ResultadoValidacion {
        val dias = texto.toIntOrNull()
        return when {
            texto.isBlank() -> ResultadoValidacion.error("La frecuencia es obligatoria")
            dias == null -> ResultadoValidacion.error("Ingresa solo números")
            dias !in 1..30 -> ResultadoValidacion.error("Debe estar entre 1 y 30 días")
            else -> ResultadoValidacion.OK
        }
    }
}