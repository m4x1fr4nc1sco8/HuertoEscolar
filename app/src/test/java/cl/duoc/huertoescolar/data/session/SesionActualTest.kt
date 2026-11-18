package cl.duoc.huertoescolar.data.session

import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SesionActualTest {

    // SesionActual es un object (estado global): se limpia antes y después de cada test
    @Before
    fun limpiarAntes() {
        SesionActual.cerrar()
    }

    @After
    fun limpiarDespues() {
        SesionActual.cerrar()
    }

    private fun usuario(correo: String, tipo: Rol) = Usuario(
        nombre = "Ana",
        apellido = "Soto",
        correo = correo,
        fechaNacimiento = "01/01/2000",
        clave = "12345678",
        tipoUsuario = tipo.name
    )

    @Test
    fun usuario_sinSesionIniciada_esNull() {
        // ARRANGE + ACT
        val actual = SesionActual.usuario.value

        // ASSERT
        assertNull(actual)
    }

    @Test
    fun iniciar_conUnUsuario_loDejaComoUsuarioActual() {
        // ARRANGE
        val admin = usuario("admin@huerto.cl", Rol.ADMINISTRADOR)

        // ACT
        SesionActual.iniciar(admin)

        // ASSERT
        assertEquals(admin, SesionActual.usuario.value)
    }

    @Test
    fun iniciar_conOtroUsuario_reemplazaAlAnterior() {
        // ARRANGE
        val admin = usuario("admin@huerto.cl", Rol.ADMINISTRADOR)
        val estudiante = usuario("estudiante@huerto.cl", Rol.ESTUDIANTE)
        SesionActual.iniciar(admin)

        // ACT
        SesionActual.iniciar(estudiante)

        // ASSERT
        assertEquals(estudiante, SesionActual.usuario.value)
    }

    @Test
    fun cerrar_conSesionIniciada_dejaElUsuarioEnNull() {
        // ARRANGE
        SesionActual.iniciar(usuario("admin@huerto.cl", Rol.ADMINISTRADOR))

        // ACT
        SesionActual.cerrar()

        // ASSERT
        assertNull(SesionActual.usuario.value)
    }
}
