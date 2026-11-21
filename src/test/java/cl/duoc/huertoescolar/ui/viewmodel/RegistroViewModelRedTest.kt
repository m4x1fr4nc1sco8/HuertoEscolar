package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.errorHttp
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Qué pasa en el registro cuando el servidor falla
class RegistroViewModelRedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: UsuarioRepository
    private lateinit var viewModel: RegistroViewModel

    @Before
    fun preparar() {
        repositorio = mockk()
        viewModel = RegistroViewModel(repositorio)
        viewModel.cambiarNombre("Ana")
        viewModel.cambiarApellido("Soto")
        viewModel.cambiarCorreo("ana@huerto.cl")
        viewModel.cambiarFecha("05/07/2014")
        viewModel.cambiarClave("12345678")
        viewModel.cambiarRepetir("12345678")
        viewModel.cambiarTerminos(true)
    }

    @Test
    fun registrar_sinConexionConElServidor_avisaYConservaElFormulario() {
        // ARRANGE
        coEvery { repositorio.guardar(any()) } throws IOException("sin red")

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("No se pudo conectar con el servidor. Comprueba la conexión.", estado.mensaje)
        assertEquals("ana@huerto.cl", estado.correo)
    }

    @Test
    fun registrar_servidorRechazaElCorreo_muestraElMensajeDelServidor() {
        // ARRANGE
        coEvery { repositorio.guardar(any()) } throws errorHttp(409, "Ya existe un usuario con ese correo")

        // ACT
        viewModel.registrar(permitirAdministrador = false)

        // ASSERT
        assertEquals("Ya existe un usuario con ese correo", viewModel.uiState.value.mensaje)
    }
}
