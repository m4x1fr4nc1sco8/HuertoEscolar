package cl.duoc.huertoescolar.ui.viewmodel


import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: UsuarioRepository
    private lateinit var viewModel: RegistroViewModel
    private val guardado = slot<Usuario>()

    @Before
    fun preparar() {
        repositorio = mockk()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        viewModel = RegistroViewModel(repositorio)
    }

    private fun fechaHaceAnios(anios: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.YEAR, -anios)
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendario.time)
    }

    // Deja el formulario listo para guardar; cada test cambia solo lo que quiere probar
    private fun llenarFormularioValido(fecha: String = "05/07/2014") {
        viewModel.cambiarNombre("Ana")
        viewModel.cambiarApellido("Soto")
        viewModel.cambiarCorreo("ana@huerto.cl")
        viewModel.cambiarFecha(fecha)
        viewModel.cambiarClave("huerto1234")
        viewModel.cambiarRepetir("huerto1234")
        viewModel.cambiarTerminos(true)
    }

    @Test
    fun cambiar_camposConErroresPrevios_actualizanElValorYLimpianSuError() {
        // ARRANGE: el formulario vacío provoca todos los errores
        viewModel.registrar(permitirAdministrador = false)
        assertEquals("El nombre es obligatorio", viewModel.uiState.value.errorNombre)

        // ACT
        viewModel.cambiarNombre("Ana")
        viewModel.cambiarApellido("Soto")
        viewModel.cambiarCorreo("ana@huerto.cl")
        viewModel.cambiarFecha("05/07/2014")
        viewModel.cambiarClave("huerto1234")
        viewModel.cambiarRepetir("huerto1234")
        viewModel.cambiarEsAdministrador(true)
        viewModel.cambiarTerminos(true)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("Ana", estado.nombre)
        assertEquals("Soto", estado.apellido)
        assertEquals("ana@huerto.cl", estado.correo)
        assertEquals("05/07/2014", estado.fechaNacimiento)
        assertEquals("huerto1234", estado.clave)
        assertEquals("huerto1234", estado.repetirClave)
        assertTrue(estado.esAdministrador)
        assertTrue(estado.aceptaTerminos)
        assertNull(estado.errorNombre)
        assertNull(estado.errorApellido)
        assertNull(estado.errorCorreo)
        assertNull(estado.errorFecha)
        assertNull(estado.errorClave)
        assertNull(estado.errorRepetir)
        assertNull(estado.errorTerminos)
    }

    @Test
    fun registrar_formularioVacio_muestraTodosLosErroresYNoGuarda() {
        // ARRANGE: formulario vacío

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("El nombre es obligatorio", estado.errorNombre)
        assertEquals("El apellido es obligatorio", estado.errorApellido)
        assertEquals("El correo es obligatorio", estado.errorCorreo)
        assertEquals("La fecha de nacimiento es obligatoria", estado.errorFecha)
        assertEquals("La contraseña es obligatoria", estado.errorClave)
        assertEquals("Repite la contraseña", estado.errorRepetir)
        assertEquals("Debes aceptar los términos", estado.errorTerminos)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun registrar_sinAceptarLosTerminos_muestraErrorYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarTerminos(false)

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        assertEquals("Debes aceptar los términos", viewModel.uiState.value.errorTerminos)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun registrar_clavesDistintas_muestraErrorEnRepetirYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarRepetir("otraClave99")

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorRepetir)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun registrar_estudianteMenorDe4Anios_muestraErrorDeFechaYNoGuarda() {
        // ARRANGE
        llenarFormularioValido(fecha = fechaHaceAnios(2))

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        assertEquals(
            "El perfil estudiante requiere tener al menos 4 años",
            viewModel.uiState.value.errorFecha
        )
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun registrar_administradorMenorDeEdad_muestraErrorDeFechaYNoGuarda() {
        // ARRANGE
        llenarFormularioValido(fecha = "05/07/2014")
        viewModel.cambiarEsAdministrador(true)

        // ACT
        viewModel.registrar(permitirAdministrador = true)

        // ASSERT
        assertEquals(
            "El perfil administrador requiere ser mayor de 18 años",
            viewModel.uiState.value.errorFecha
        )
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun registrar_estudianteValido_guardaUsuarioSinEspaciosYLimpiaElFormulario() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarNombre("  Ana ")
        viewModel.cambiarCorreo(" ana@huerto.cl ")

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        val usuario = guardado.captured
        assertEquals("Ana", usuario.nombre)
        assertEquals("Soto", usuario.apellido)
        assertEquals("ana@huerto.cl", usuario.correo)
        assertEquals("05/07/2014", usuario.fechaNacimiento)
        assertEquals("huerto1234", usuario.clave)
        assertEquals(Rol.ESTUDIANTE.name, usuario.tipoUsuario)
        assertEquals(
            RegistroUiState(mensaje = "Usuario registrado correctamente"),
            viewModel.uiState.value
        )
    }

    @Test
    fun registrar_administradorConPermiso_guardaConPerfilAdministrador() {
        // ARRANGE
        llenarFormularioValido(fecha = "12/03/1988")
        viewModel.cambiarEsAdministrador(true)

        // ACT
        viewModel.registrar(permitirAdministrador = true)

        // ASSERT
        assertEquals(Rol.ADMINISTRADOR.name, guardado.captured.tipoUsuario)
    }

    @Test
    fun registrar_administradorSinPermiso_guardaComoEstudiante() {
        // ARRANGE: el usuario marcó "administrador", pero quien registra no tiene permiso
        llenarFormularioValido(fecha = "05/07/2014")
        viewModel.cambiarEsAdministrador(true)

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        assertEquals(Rol.ESTUDIANTE.name, guardado.captured.tipoUsuario)
        assertFalse(viewModel.uiState.value.esAdministrador)
    }
}
