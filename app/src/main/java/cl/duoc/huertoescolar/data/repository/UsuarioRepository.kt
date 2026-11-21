package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.UsuarioDao
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.aSolicitud
import cl.duoc.huertoescolar.data.remote.aUsuario
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val api: HuertoApi
) {
    // Registra al usuario en el servidor y guarda la copia con el id que le asignó
    suspend fun guardar(usuario: Usuario) {
        val creado = api.crearUsuario(usuario.aSolicitud())
        usuarioDao.insertar(usuario.copy(id = creado.id))
    }

    fun obtenerUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.obtenerUsuarios()
    }

    // Solo los usuarios con perfil estudiante (para asignarles cultivos)
    fun obtenerEstudiantes(): Flow<List<Usuario>> {
        return usuarioDao.obtenerPorTipo(Rol.ESTUDIANTE.name)
    }

    suspend fun buscarPorCorreo(correo: String): Usuario? {
        return usuarioDao.buscarPorCorreo(correo)
    }

    suspend fun sembrarCuentasDePrueba() {
        if (usuarioDao.buscarPorCorreo("admin@huerto.cl") == null) {
            usuarioDao.insertar(
                Usuario(
                    nombre = "Camila",
                    apellido = "Torres",
                    correo = "admin@huerto.cl",
                    fechaNacimiento = "12/03/1988",
                    clave = "huerto1234",
                    tipoUsuario = Rol.ADMINISTRADOR.name
                )
            )
        }
        if (usuarioDao.buscarPorCorreo("estudiante@huerto.cl") == null) {
            usuarioDao.insertar(
                Usuario(
                    nombre = "Pedro",
                    apellido = "Saez",
                    correo = "estudiante@huerto.cl",
                    fechaNacimiento = "05/07/2014",
                    clave = "huerto1234",
                    tipoUsuario = Rol.ESTUDIANTE.name
                )
            )
        }
    }

    // Deja Room igual que el servidor (mismos ids) para que los cultivos apunten al
    // estudiante correcto. La clave nunca viaja desde el servidor: se conserva la
    // que ya había en el dispositivo (las cuentas solo del servidor quedan sin clave local).
    suspend fun sincronizar() {
        val remotos = api.obtenerUsuarios()
        val locales = usuarioDao.listar()
        val correosRemotos = remotos.map { it.correo.lowercase() }.toSet()

        // Cuentas creadas solo en este dispositivo: se suben al servidor
        val subidos = locales
            .filter { it.correo.lowercase() !in correosRemotos }
            .map { api.crearUsuario(it.aSolicitud()) }

        val clavesLocales = locales.associate { it.correo.lowercase() to it.clave }
        val espejo = (remotos + subidos).map { remoto ->
            remoto.aUsuario(clave = clavesLocales[remoto.correo.lowercase()] ?: "")
        }
        usuarioDao.eliminarTodos()
        usuarioDao.insertarTodos(espejo)
    }
}
