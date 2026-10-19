package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.model.Rol
import cl.duoc.huertoescolar.model.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun guardar(usuario: Usuario) {
        usuarioDao.insertar(usuario)
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
}