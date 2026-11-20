package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsuariosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun usuario(correo: String) = Usuario(
        nombre = "Ana",
        apellido = "Soto",
        correo = correo,
        fechaNacimiento = "01/01/2000",
        clave = "12345678",
        tipoUsuario = Rol.ESTUDIANTE.name
    )

    @Test
    fun usuarios_sinNingunSuscriptor_empiezaConListaVacia() {
        // ARRANGE
        val repositorio = mockk<UsuarioRepository>()
        every { repositorio.obtenerUsuarios() } returns flowOf(listOf(usuario("a@huerto.cl")))

        // ACT
        val viewModel = UsuariosViewModel(repositorio)

        // ASSERT
        assertEquals(emptyList<Usuario>(), viewModel.usuarios.value)
    }

    @Test
    fun usuarios_conUnSuscriptor_emiteLosUsuariosDelRepositorio() = runTest {
        // ARRANGE
        val lista = listOf(usuario("a@huerto.cl"), usuario("b@huerto.cl"))
        val repositorio = mockk<UsuarioRepository>()
        every { repositorio.obtenerUsuarios() } returns flowOf(lista)
        val viewModel = UsuariosViewModel(repositorio)

        // ACT: al suscribirse, el StateFlow empieza a leer el repositorio
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.usuarios.collect {}
        }

        // ASSERT
        assertEquals(lista, viewModel.usuarios.value)
    }
}
