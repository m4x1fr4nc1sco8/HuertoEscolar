package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.UsuarioDao
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryTest {

    private lateinit var dao: UsuarioDao
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        dao = mockk()
        repositorio = UsuarioRepository(dao)
    }

    private fun usuario(correo: String = "ana@huerto.cl", tipo: Rol = Rol.ESTUDIANTE) = Usuario(
        nombre = "Ana",
        apellido = "Soto",
        correo = correo,
        fechaNacimiento = "01/01/2010",
        clave = "12345678",
        tipoUsuario = tipo.name
    )

    @Test
    fun guardar_usuarioValido_loInsertaEnElDao() = runTest {
        // ARRANGE
        val ana = usuario()
        coEvery { dao.insertar(ana) } just Runs

        // ACT
        repositorio.guardar(ana)

        // ASSERT
        coVerify(exactly = 1) { dao.insertar(ana) }
    }

    @Test
    fun obtenerUsuarios_daoConUsuarios_emiteLaMismaLista() = runTest {
        // ARRANGE
        val lista = listOf(usuario("a@huerto.cl"), usuario("b@huerto.cl"))
        every { dao.obtenerUsuarios() } returns flowOf(lista)

        // ACT
        val resultado = repositorio.obtenerUsuarios().first()

        // ASSERT
        assertEquals(lista, resultado)
    }

    @Test
    fun obtenerEstudiantes_consultaElDaoConElTipoEstudiante() = runTest {
        // ARRANGE
        val estudiantes = listOf(usuario())
        every { dao.obtenerPorTipo("ESTUDIANTE") } returns flowOf(estudiantes)

        // ACT
        val resultado = repositorio.obtenerEstudiantes().first()

        // ASSERT
        assertEquals(estudiantes, resultado)
        verify(exactly = 1) { dao.obtenerPorTipo("ESTUDIANTE") }
    }

    @Test
    fun buscarPorCorreo_correoExistente_retornaElUsuario() = runTest {
        // ARRANGE
        val ana = usuario()
        coEvery { dao.buscarPorCorreo("ana@huerto.cl") } returns ana

        // ACT
        val resultado = repositorio.buscarPorCorreo("ana@huerto.cl")

        // ASSERT
        assertEquals(ana, resultado)
    }

    @Test
    fun buscarPorCorreo_correoInexistente_retornaNull() = runTest {
        // ARRANGE
        coEvery { dao.buscarPorCorreo("nadie@huerto.cl") } returns null

        // ACT
        val resultado = repositorio.buscarPorCorreo("nadie@huerto.cl")

        // ASSERT
        assertNull(resultado)
    }

    @Test
    fun sembrarCuentasDePrueba_baseVacia_insertaAdministradorYEstudiante() = runTest {
        // ARRANGE
        val insertados = mutableListOf<Usuario>()
        coEvery { dao.buscarPorCorreo(any()) } returns null
        coEvery { dao.insertar(capture(insertados)) } just Runs

        // ACT
        repositorio.sembrarCuentasDePrueba()

        // ASSERT
        assertEquals(2, insertados.size)
        assertEquals("admin@huerto.cl", insertados[0].correo)
        assertEquals(Rol.ADMINISTRADOR.name, insertados[0].tipoUsuario)
        assertEquals("estudiante@huerto.cl", insertados[1].correo)
        assertEquals(Rol.ESTUDIANTE.name, insertados[1].tipoUsuario)
    }

    @Test
    fun sembrarCuentasDePrueba_cuentasYaExisten_noInsertaNada() = runTest {
        // ARRANGE
        coEvery { dao.buscarPorCorreo("admin@huerto.cl") } returns
                usuario("admin@huerto.cl", Rol.ADMINISTRADOR)
        coEvery { dao.buscarPorCorreo("estudiante@huerto.cl") } returns
                usuario("estudiante@huerto.cl", Rol.ESTUDIANTE)

        // ACT
        repositorio.sembrarCuentasDePrueba()

        // ASSERT
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun sembrarCuentasDePrueba_soloExisteAdministrador_insertaSoloElEstudiante() = runTest {
        // ARRANGE
        val insertados = mutableListOf<Usuario>()
        coEvery { dao.buscarPorCorreo("admin@huerto.cl") } returns
                usuario("admin@huerto.cl", Rol.ADMINISTRADOR)
        coEvery { dao.buscarPorCorreo("estudiante@huerto.cl") } returns null
        coEvery { dao.insertar(capture(insertados)) } just Runs

        // ACT
        repositorio.sembrarCuentasDePrueba()

        // ASSERT
        assertEquals(1, insertados.size)
        assertEquals("estudiante@huerto.cl", insertados[0].correo)
    }

    @Test
    fun sembrarCuentasDePrueba_soloExisteEstudiante_insertaSoloElAdministrador() = runTest {
        // ARRANGE
        val insertados = mutableListOf<Usuario>()
        coEvery { dao.buscarPorCorreo("admin@huerto.cl") } returns null
        coEvery { dao.buscarPorCorreo("estudiante@huerto.cl") } returns
                usuario("estudiante@huerto.cl", Rol.ESTUDIANTE)
        coEvery { dao.insertar(capture(insertados)) } just Runs

        // ACT
        repositorio.sembrarCuentasDePrueba()

        // ASSERT
        assertEquals(1, insertados.size)
        assertEquals("admin@huerto.cl", insertados[0].correo)
    }
}
