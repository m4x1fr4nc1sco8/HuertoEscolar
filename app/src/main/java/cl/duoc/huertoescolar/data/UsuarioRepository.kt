package cl.duoc.huertoescolar.data

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
}