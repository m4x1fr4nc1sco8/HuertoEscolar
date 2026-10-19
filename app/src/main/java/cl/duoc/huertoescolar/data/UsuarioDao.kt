package cl.duoc.huertoescolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.huertoescolar.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    fun obtenerUsuarios(): Flow<List<Usuario>>

    // Usuarios de un tipo (nombre de Rol), ordenados por nombre
    @Query("SELECT * FROM usuarios WHERE tipoUsuario = :tipo ORDER BY nombre, apellido")
    fun obtenerPorTipo(tipo: String): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
}
