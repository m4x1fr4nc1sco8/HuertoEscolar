package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: UsuarioRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        // SesionActual es un object (estado global): se limpia para que un test no afecte a otro
        SesionActual.cerrar()
        repositorio = mockk()
        coEvery { repositorio.sembrarCuentasDePrueba() } just Runs
        viewModel = LoginViewModel(repositorio)
    }

    @After
    fun limpiar() {
        SesionActual.cerrar()
    }

    private fun admin() = Usuario(
        id = 1,
        nombre = "Camila",
        apellido = "Torres",
        correo = "admin@huerto.cl",
        fechaNacimiento = "12/03/1988",
        clave = "huerto1234",
        tipoUsuario = Rol.ADMINISTRADOR.name
    )

    @Test
    fun init_alCrearseElViewModel_siembraLasCuentasDePrueba() {
        // ARRANGE + ACT: el ViewModel ya se creó en preparar()

        // ASSERT
        coVerify(exactly = 1) { repositorio.sembrarCuentasDePrueba() }
    }

    @Test
    fun cambiarCorreo_conErroresPrevios_actualizaElCorreoYLimpiaLosErrores() {
        // ARRANGE: el formulario vacío provoca errores
        viewModel.ingresar {}
        assertNotNull(viewModel.uiState.value.errorCorreo)

        // ACT
        viewModel.cambiarCorreo("ana@huerto.cl")

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("ana@huerto.cl", estado.correo)
        assertNull(estado.errorCorreo)
        assertNull(estado.errorGeneral)
    }

    @Test
    fun cambiarClave_conErroresPrevios_actualizaLaClaveYLimpiaLosErrores() {
        // ARRANGE
        viewModel.ingresar {}
        assertNotNull(viewModel.uiState.value.errorClave)

        // ACT
        viewModel.cambiarClave("huerto1234")

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("huerto1234", estado.clave)
        assertNull(estado.errorClave)
        assertNull(estado.errorGeneral)
    }

    @Test
    fun ingresar_camposVacios_muestraErroresYNoConsultaElRepositorio() {
        // ARRANGE
        var exitoso = false

        // ACT
        viewModel.ingresar { exitoso = true }

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("El correo es obligatorio", estado.errorCorreo)
        assertEquals("La contraseña es obligatoria", estado.errorClave)
        assertFalse(exitoso)
        coVerify(exactly = 0) { repositorio.buscarPorCorreo(any()) }
    }

    @Test
    fun ingresar_correoInvalido_muestraErrorDeCorreoYNoConsultaElRepositorio() {
        // ARRANGE
        viewModel.cambiarCorreo("sin-arroba")
        viewModel.cambiarClave("huerto1234")

        // ACT
        viewModel.ingresar {}

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("Ingresa un correo válido", estado.errorCorreo)
        assertNull(estado.errorClave)
        coVerify(exactly = 0) { repositorio.buscarPorCorreo(any()) }
    }

    @Test
    fun ingresar_claveMuyCorta_muestraErrorDeClaveYNoConsultaElRepositorio() {
        // ARRANGE
        viewModel.cambiarCorreo("admin@huerto.cl")
        viewModel.cambiarClave("123")

        // ACT
        viewModel.ingresar {}

        // ASSERT
        val estado = viewModel.uiState.value
        assertNull(estado.errorCorreo)
        assertEquals("Mínimo 8 caracteres", estado.errorClave)
        coVerify(exactly = 0) { repositorio.buscarPorCorreo(any()) }
    }

    @Test
    fun ingresar_credencialesCorrectas_iniciaSesionLlamaOnExitosoYLimpiaElFormulario() {
        // ARRANGE
        val usuario = admin()
        coEvery { repositorio.buscarPorCorreo("admin@huerto.cl") } returns usuario
        viewModel.cambiarCorreo("admin@huerto.cl")
        viewModel.cambiarClave("huerto1234")
        var exitoso = false

        // ACT
        viewModel.ingresar { exitoso = true }

        // ASSERT
        assertTrue(exitoso)
        assertEquals(usuario, SesionActual.usuario.value)
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun ingresar_correoConEspacios_buscaElUsuarioConElCorreoSinEspacios() {
        // ARRANGE
        coEvery { repositorio.buscarPorCorreo("admin@huerto.cl") } returns admin()
        viewModel.cambiarCorreo("  admin@huerto.cl  ")
        viewModel.cambiarClave("huerto1234")

        // ACT
        viewModel.ingresar {}

        // ASSERT
        coVerify(exactly = 1) { repositorio.buscarPorCorreo("admin@huerto.cl") }
    }

    @Test
    fun ingresar_claveIncorrecta_muestraErrorGeneralYNoIniciaSesion() {
        // ARRANGE
        coEvery { repositorio.buscarPorCorreo("admin@huerto.cl") } returns admin()
        viewModel.cambiarCorreo("admin@huerto.cl")
        viewModel.cambiarClave("otraClave99")
        var exitoso = false

        // ACT
        viewModel.ingresar { exitoso = true }

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("Correo o contraseña incorrectos", estado.errorGeneral)
        assertFalse(estado.cargando)
        assertFalse(exitoso)
        assertNull(SesionActual.usuario.value)
    }

    @Test
    fun ingresar_usuarioInexistente_muestraErrorGeneralYNoIniciaSesion() {
        // ARRANGE
        coEvery { repositorio.buscarPorCorreo("nadie@huerto.cl") } returns null
        viewModel.cambiarCorreo("nadie@huerto.cl")
        viewModel.cambiarClave("huerto1234")
        var exitoso = false

        // ACT
        viewModel.ingresar { exitoso = true }

        // ASSERT
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.errorGeneral)
        assertFalse(exitoso)
        assertNull(SesionActual.usuario.value)
    }
}
