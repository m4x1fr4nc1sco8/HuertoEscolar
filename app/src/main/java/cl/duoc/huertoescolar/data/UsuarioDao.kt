package cl.duoc.huertoescolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import cl.duoc.huertoescolar.model.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario)
}