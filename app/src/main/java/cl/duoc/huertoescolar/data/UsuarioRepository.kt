package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.model.Usuario

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun guardar(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }
}