package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.UsuarioDao
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.UsuarioDto
import cl.duoc.huertoescolar.data.remote.UsuarioSolicitud
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException

class UsuarioRepositoryTest {

    private lateinit var dao: UsuarioDao
    private lateinit var api: HuertoApi
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        dao = mockk()
        api = mockk()
        repositorio = UsuarioRepository(dao, api)
    }

    private fun usuario(
        correo: String = "ana@huerto.cl",
        tipo: Rol = Rol.ESTUDIANTE,
        id: Int = 0,
        clave: String = "12345678"
    ) = Usuario(
        id = id,
        nombre = "Ana",
        apellido = "Soto",
        correo = correo,
        fechaNacimiento = "01/01/2010",
        clave = clave,
        tipoUsuario = tipo.name
    )

    private fun dto(id: Int, correo: String = "ana@huerto.cl", rol: Rol = Rol.ESTUDIANTE) = UsuarioDto(
        id = id,
        nombre = "Ana",
        apellido = "Soto",
        correo = correo,
        fechaNacimiento = "2010-01-01",
        rol = rol.name
    )

    // ---------- registrar ----------

    @Test
    fun guardar_servidorCreaElUsuario_loGuardaEnElDaoConLaIdDelServidor() = runTest {
        // ARRANGE
        val ana = usuario()
        coEvery {
            api.crearUsuario(
                UsuarioSolicitud("Ana", "Soto", "ana@huerto.cl", "2010-01-01", "12345678", "ESTUDIANTE")
            )
        } returns dto(id = 8)
        coEvery { dao.insertar(any()) } just Runs

        // ACT
        repositorio.guardar(ana)

        // ASSERT
        coVerify(exactly = 1) { dao.insertar(ana.copy(id = 8)) }
    }

    @Test
    fun guardar_servidorRechaza_lanzaLaExcepcionYNoInsertaEnElDao() {
        // ARRANGE
        coEvery { api.crearUsuario(any()) } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.guardar(usuario()) } }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    // ---------- leer (siempre desde Room) ----------

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
        coEvery { dao.buscarPorCorreo("nadie@huerto.cl") } returns null

        assertNull(repositorio.buscarPorCorreo("nadie@huerto.cl"))
    }

    // ---------- cuentas de prueba ----------

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

    // ---------- sincronizar ----------

    @Test
    fun sincronizar_usuarioConOtraIdEnElServidor_tomaLaIdDelServidorYConservaLaClaveLocal() = runTest {
        // ARRANGE: en el dispositivo Ana tiene id 1; en el servidor es la 5
        coEvery { api.obtenerUsuarios() } returns listOf(dto(id = 5))
        coEvery { dao.listar() } returns listOf(usuario(id = 1, clave = "claveSecreta1"))
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT
        coVerifyOrder {
            dao.eliminarTodos()
            dao.insertarTodos(listOf(usuario(id = 5, clave = "claveSecreta1")))
        }
        coVerify(exactly = 0) { api.crearUsuario(any()) }
    }

    @Test
    fun sincronizar_usuarioSoloEnElServidor_loGuardaSinClaveLocal() = runTest {
        // ARRANGE
        coEvery { api.obtenerUsuarios() } returns listOf(dto(id = 3, correo = "otro@huerto.cl"))
        coEvery { dao.listar() } returns emptyList()
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT
        coVerify(exactly = 1) {
            dao.insertarTodos(listOf(usuario(correo = "otro@huerto.cl", id = 3, clave = "")))
        }
    }

    @Test
    fun sincronizar_usuarioSoloEnElDispositivo_loSubeAlServidorYConservaSuClave() = runTest {
        // ARRANGE
        val local = usuario(correo = "nuevo@huerto.cl", id = 4, clave = "miClave123")
        coEvery { api.obtenerUsuarios() } returns emptyList()
        coEvery { dao.listar() } returns listOf(local)
        coEvery { api.crearUsuario(any()) } returns dto(id = 10, correo = "nuevo@huerto.cl")
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT
        coVerify(exactly = 1) {
            api.crearUsuario(
                UsuarioSolicitud("Ana", "Soto", "nuevo@huerto.cl", "2010-01-01", "miClave123", "ESTUDIANTE")
            )
        }
        coVerify(exactly = 1) {
            dao.insertarTodos(listOf(usuario(correo = "nuevo@huerto.cl", id = 10, clave = "miClave123")))
        }
    }

    @Test
    fun sincronizar_comparaLosCorreosSinDistinguirMayusculas() = runTest {
        // ARRANGE
        coEvery { api.obtenerUsuarios() } returns listOf(dto(id = 5, correo = "ana@huerto.cl"))
        coEvery { dao.listar() } returns listOf(usuario(correo = "Ana@Huerto.cl", id = 1))
        coEvery { dao.eliminarTodos() } just Runs
        coEvery { dao.insertarTodos(any()) } just Runs

        // ACT
        repositorio.sincronizar()

        // ASSERT: no se vuelve a subir porque ya existe
        coVerify(exactly = 0) { api.crearUsuario(any()) }
    }

    @Test
    fun sincronizar_servidorNoResponde_conservaLaCopiaLocal() {
        // ARRANGE
        coEvery { api.obtenerUsuarios() } throws IOException("sin red")

        // ACT + ASSERT
        assertThrows(IOException::class.java) { runBlocking { repositorio.sincronizar() } }
        coVerify(exactly = 0) { dao.eliminarTodos() }
        coVerify(exactly = 0) { dao.insertarTodos(any()) }
    }
}
