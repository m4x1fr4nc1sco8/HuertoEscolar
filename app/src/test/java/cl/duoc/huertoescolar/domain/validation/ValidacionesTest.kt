package cl.duoc.huertoescolar.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ValidacionesTest {

    // ---------- Helpers ----------

    private fun assertValido(resultado: ResultadoValidacion) {
        assertTrue("Se esperaba un resultado válido, pero fue: ${resultado.mensaje}", resultado.esValido)
        assertNull(resultado.mensaje)
    }

    private fun assertError(mensajeEsperado: String, resultado: ResultadoValidacion) {
        assertFalse("Se esperaba un error, pero el resultado fue válido", resultado.esValido)
        assertEquals(mensajeEsperado, resultado.mensaje)
    }

    // Fecha de hoy desplazada en años y días, con el formato dd/MM/yyyy de la app
    private fun fecha(anios: Int = 0, dias: Int = 0): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.YEAR, anios)
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendario.time)
    }

    // ---------- ResultadoValidacion ----------

    @Test
    fun `OK es un resultado valido sin mensaje`() {
        assertValido(ResultadoValidacion.OK)
    }

    @Test
    fun `error crea un resultado invalido con su mensaje`() {
        assertError("algo salio mal", ResultadoValidacion.error("algo salio mal"))
    }

    // ---------- validarObligatorio ----------

    @Test
    fun `obligatorio vacio devuelve error con el nombre del campo`() {
        assertError("La ubicación es obligatorio", Validaciones.validarObligatorio("", "La ubicación"))
    }

    @Test
    fun `obligatorio solo con espacios devuelve error`() {
        assertError("El código es obligatorio", Validaciones.validarObligatorio("   ", "El código"))
    }

    @Test
    fun `obligatorio con texto es valido`() {
        assertValido(Validaciones.validarObligatorio("patio norte", "La ubicación"))
    }

    // ---------- validarNombre ----------

    @Test
    fun `nombre vacio devuelve error de obligatorio`() {
        assertError("El nombre es obligatorio", Validaciones.validarNombre(""))
    }

    @Test
    fun `nombre con menos de 3 caracteres devuelve error`() {
        assertError("El nombre debe tener al menos 3 caracteres", Validaciones.validarNombre("Al"))
    }

    @Test
    fun `nombre con espacios alrededor se evalua sin ellos`() {
        assertError("El nombre debe tener al menos 3 caracteres", Validaciones.validarNombre("  Al "))
    }

    @Test
    fun `nombre de 3 caracteres es valido`() {
        assertValido(Validaciones.validarNombre("Ana"))
    }

    @Test
    fun `nombre usa el campo personalizado en el mensaje`() {
        assertError(
            "El nombre del cultivo es obligatorio",
            Validaciones.validarNombre("", "El nombre del cultivo")
        )
    }

    // ---------- validarCorreo ----------

    @Test
    fun `correo vacio devuelve error de obligatorio`() {
        assertError("El correo es obligatorio", Validaciones.validarCorreo(""))
    }

    @Test
    fun `correo sin arroba es invalido`() {
        assertError("Ingresa un correo válido", Validaciones.validarCorreo("usuario.huerto.cl"))
    }

    @Test
    fun `correo que no termina en cl devuelve error`() {
        assertError("El correo debe terminar en .cl", Validaciones.validarCorreo("usuario@huerto.com"))
    }

    @Test
    fun `correo valido con terminacion cl es aceptado`() {
        assertValido(Validaciones.validarCorreo("admin@huerto.cl"))
    }

    @Test
    fun `correo con espacios alrededor se acepta si el resto es valido`() {
        assertValido(Validaciones.validarCorreo("  admin@huerto.cl "))
    }

    // ---------- validarClave ----------

    @Test
    fun `clave vacia devuelve error de obligatoria`() {
        assertError("La contraseña es obligatoria", Validaciones.validarClave(""))
    }

    @Test
    fun `clave de 7 caracteres es demasiado corta`() {
        assertError("Mínimo 8 caracteres", Validaciones.validarClave("1234567"))
    }

    @Test
    fun `clave de 8 caracteres es valida`() {
        assertValido(Validaciones.validarClave("12345678"))
    }

    // ---------- validarClavesIguales ----------

    @Test
    fun `repetir clave vacio pide repetir la contrasena`() {
        assertError("Repite la contraseña", Validaciones.validarClavesIguales("huerto1234", ""))
    }

    @Test
    fun `claves distintas devuelven error de que no coinciden`() {
        assertError(
            "Las contraseñas no coinciden",
            Validaciones.validarClavesIguales("huerto1234", "huerto4321")
        )
    }

    @Test
    fun `claves iguales son validas`() {
        assertValido(Validaciones.validarClavesIguales("huerto1234", "huerto1234"))
    }

    // ---------- validarFrecuenciaRiego ----------

    @Test
    fun `frecuencia vacia devuelve error de obligatoria`() {
        assertError("La frecuencia es obligatoria", Validaciones.validarFrecuenciaRiego(""))
    }

    @Test
    fun `frecuencia con letras devuelve error de solo numeros`() {
        assertError("Ingresa solo números", Validaciones.validarFrecuenciaRiego("abc"))
    }

    @Test
    fun `frecuencia decimal devuelve error de solo numeros`() {
        assertError("Ingresa solo números", Validaciones.validarFrecuenciaRiego("2.5"))
    }

    @Test
    fun `frecuencia cero esta fuera de rango`() {
        assertError("Debe estar entre 1 y 30 días", Validaciones.validarFrecuenciaRiego("0"))
    }

    @Test
    fun `frecuencia negativa esta fuera de rango`() {
        assertError("Debe estar entre 1 y 30 días", Validaciones.validarFrecuenciaRiego("-5"))
    }

    @Test
    fun `frecuencia de 31 esta fuera de rango`() {
        assertError("Debe estar entre 1 y 30 días", Validaciones.validarFrecuenciaRiego("31"))
    }

    @Test
    fun `frecuencia en los limites 1 y 30 es valida`() {
        assertValido(Validaciones.validarFrecuenciaRiego("1"))
        assertValido(Validaciones.validarFrecuenciaRiego("30"))
    }

    // ---------- validarFecha ----------

    @Test
    fun `fecha vacia devuelve error de obligatoria con el campo`() {
        assertError("La fecha es obligatoria", Validaciones.validarFecha(""))
        assertError(
            "La fecha de siembra es obligatoria",
            Validaciones.validarFecha("  ", "La fecha de siembra")
        )
    }

    @Test
    fun `fecha con formato incorrecto pide dd mm aaaa`() {
        assertError("Usa el formato dd/mm/aaaa", Validaciones.validarFecha("1/3/2025"))
        assertError("Usa el formato dd/mm/aaaa", Validaciones.validarFecha("2025-03-01"))
        assertError("Usa el formato dd/mm/aaaa", Validaciones.validarFecha("hola"))
    }

    @Test
    fun `fecha inexistente devuelve error`() {
        assertError("La fecha no existe", Validaciones.validarFecha("31/02/2025"))
        assertError("La fecha no existe", Validaciones.validarFecha("15/13/2025"))
        assertError("La fecha no existe", Validaciones.validarFecha("31/04/2025"))
    }

    @Test
    fun `29 de febrero solo existe en anios bisiestos`() {
        assertValido(Validaciones.validarFecha("29/02/2024"))
        assertError("La fecha no existe", Validaciones.validarFecha("29/02/2023"))
    }

    @Test
    fun `fecha valida es aceptada incluso con espacios alrededor`() {
        assertValido(Validaciones.validarFecha("15/03/2025"))
        assertValido(Validaciones.validarFecha("  15/03/2025  "))
    }

    // ---------- validarCosechaPosterior ----------

    @Test
    fun `cosecha vacia devuelve error de obligatoria`() {
        assertError(
            "La fecha de cosecha es obligatoria",
            Validaciones.validarCosechaPosterior("01/03/2025", "")
        )
    }

    @Test
    fun `cosecha con formato incorrecto devuelve error de formato`() {
        assertError(
            "Usa el formato dd/mm/aaaa",
            Validaciones.validarCosechaPosterior("01/03/2025", "mañana")
        )
    }

    @Test
    fun `cosecha inexistente devuelve error`() {
        assertError(
            "La fecha no existe",
            Validaciones.validarCosechaPosterior("01/03/2025", "31/04/2025")
        )
    }

    @Test
    fun `cosecha el mismo dia de la siembra es invalida`() {
        assertError(
            "La cosecha debe ser posterior a la siembra",
            Validaciones.validarCosechaPosterior("01/03/2025", "01/03/2025")
        )
    }

    @Test
    fun `cosecha anterior a la siembra es invalida`() {
        assertError(
            "La cosecha debe ser posterior a la siembra",
            Validaciones.validarCosechaPosterior("01/03/2025", "28/02/2025")
        )
    }

    @Test
    fun `cosecha un dia despues de la siembra es valida`() {
        assertValido(Validaciones.validarCosechaPosterior("01/03/2025", "02/03/2025"))
    }

    @Test
    fun `si la siembra es invalida no se compara el orden de las fechas`() {
        assertValido(Validaciones.validarCosechaPosterior("", "02/03/2025"))
        assertValido(Validaciones.validarCosechaPosterior("no es fecha", "02/03/2025"))
    }

    // ---------- validarFechaNacimiento ----------

    @Test
    fun `nacimiento vacio devuelve error de obligatoria`() {
        assertError(
            "La fecha de nacimiento es obligatoria",
            Validaciones.validarFechaNacimiento("", requiereMayorDeEdad = true)
        )
    }

    @Test
    fun `nacimiento con formato incorrecto devuelve error de formato`() {
        assertError(
            "Usa el formato dd/mm/aaaa",
            Validaciones.validarFechaNacimiento("2000-01-01", requiereMayorDeEdad = false)
        )
    }

    @Test
    fun `nacimiento en el futuro es invalido`() {
        assertError(
            "La fecha no puede ser futura",
            Validaciones.validarFechaNacimiento(fecha(dias = 1), requiereMayorDeEdad = false)
        )
        assertError(
            "La fecha no puede ser futura",
            Validaciones.validarFechaNacimiento(fecha(dias = 1), requiereMayorDeEdad = true)
        )
    }

    @Test
    fun `administrador menor de 18 anios es invalido`() {
        assertError(
            "El perfil administrador requiere ser mayor de 18 años",
            Validaciones.validarFechaNacimiento(fecha(anios = -17), requiereMayorDeEdad = true)
        )
    }

    @Test
    fun `administrador a un dia de cumplir 18 aun es invalido`() {
        assertError(
            "El perfil administrador requiere ser mayor de 18 años",
            Validaciones.validarFechaNacimiento(fecha(anios = -18, dias = 1), requiereMayorDeEdad = true)
        )
    }

    @Test
    fun `administrador que cumple 18 hoy es valido`() {
        assertValido(Validaciones.validarFechaNacimiento(fecha(anios = -18), requiereMayorDeEdad = true))
    }

    @Test
    fun `administrador adulto es valido`() {
        assertValido(Validaciones.validarFechaNacimiento(fecha(anios = -30), requiereMayorDeEdad = true))
    }

    @Test
    fun `estudiante de 3 anios es invalido`() {
        assertError(
            "El perfil estudiante requiere tener al menos 4 años",
            Validaciones.validarFechaNacimiento(fecha(anios = -3), requiereMayorDeEdad = false)
        )
    }

    @Test
    fun `estudiante a un dia de cumplir 4 aun es invalido`() {
        assertError(
            "El perfil estudiante requiere tener al menos 4 años",
            Validaciones.validarFechaNacimiento(fecha(anios = -4, dias = 1), requiereMayorDeEdad = false)
        )
    }

    @Test
    fun `estudiante que cumple 4 hoy es valido`() {
        assertValido(Validaciones.validarFechaNacimiento(fecha(anios = -4), requiereMayorDeEdad = false))
    }

    @Test
    fun `estudiante de 10 anios es valido`() {
        assertValido(Validaciones.validarFechaNacimiento(fecha(anios = -10), requiereMayorDeEdad = false))
    }
}
