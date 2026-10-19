package cl.duoc.huertoescolar.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private val REGEX_FECHA = Regex("^\\d{2}/\\d{2}/\\d{4}$")

    fun validarClavesIguales(clave: String, repetir: String): ResultadoValidacion = when {
        repetir.isBlank() -> ResultadoValidacion.error("Repite la contraseña")
        clave != repetir -> ResultadoValidacion.error("Las contraseñas no coinciden")
        else -> ResultadoValidacion.OK
    }

    private fun parsearFecha(texto: String): Calendar? {
        return try {
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formato.isLenient = false
            val fecha = formato.parse(texto) ?: return null
            Calendar.getInstance().apply { time = fecha }
        } catch (e: ParseException) {
            null
        }
    }

    private fun calcularEdad(nacimiento: Calendar, hoy: Calendar): Int {
        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        val aunNoCumple = hoy.get(Calendar.MONTH) < nacimiento.get(Calendar.MONTH) ||
                (hoy.get(Calendar.MONTH) == nacimiento.get(Calendar.MONTH) &&
                        hoy.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH))
        if (aunNoCumple) edad--
        return edad
    }

    fun validarFecha(texto: String, campo: String = "La fecha"): ResultadoValidacion = when {
        texto.isBlank() -> ResultadoValidacion.error("$campo es obligatoria")
        !REGEX_FECHA.matches(texto.trim()) -> ResultadoValidacion.error("Usa el formato dd/mm/aaaa")
        parsearFecha(texto.trim()) == null -> ResultadoValidacion.error("La fecha no existe")
        else -> ResultadoValidacion.OK
    }

    fun validarFechaNacimiento(texto: String, requiereMayorDeEdad: Boolean): ResultadoValidacion {
        val base = validarFecha(texto, "La fecha de nacimiento")
        if (!base.esValido) return base
        val nacimiento = parsearFecha(texto.trim())
            ?: return ResultadoValidacion.error("La fecha no existe")
        val hoy = Calendar.getInstance()
        return when {
            nacimiento.after(hoy) -> ResultadoValidacion.error("La fecha no puede ser futura")
            requiereMayorDeEdad && calcularEdad(nacimiento, hoy) < 18 ->
                ResultadoValidacion.error("El perfil administrador requiere ser mayor de 18 años")
            !requiereMayorDeEdad && calcularEdad(nacimiento, hoy) < 4 ->
                ResultadoValidacion.error("El perfil estudiante requiere tener al menos 4 años")
            else -> ResultadoValidacion.OK
        }
    }
    private val REGEX_CORREO =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarObligatorio(valor: String, campo: String): ResultadoValidacion =
        if (valor.isBlank()) ResultadoValidacion.error("$campo es obligatorio")
        else ResultadoValidacion.OK

    fun validarCorreo(correo: String): ResultadoValidacion = when {
        correo.isBlank() -> ResultadoValidacion.error("El correo es obligatorio")
        !REGEX_CORREO.matches(correo.trim()) ->
            ResultadoValidacion.error("Ingresa un correo válido")
        !correo.trim().endsWith(".cl") ->
            ResultadoValidacion.error("El correo debe terminar en .cl")
        else -> ResultadoValidacion.OK
    }

    fun validarClave(clave: String): ResultadoValidacion = when {
        clave.isBlank() -> ResultadoValidacion.error("La contraseña es obligatoria")
        clave.length < 8 -> ResultadoValidacion.error("Mínimo 8 caracteres")
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