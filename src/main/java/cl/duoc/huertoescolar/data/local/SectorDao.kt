package cl.duoc.huertoescolar.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.huertoescolar.domain.model.Sector
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {

    @Insert
    suspend fun insertar(sector: Sector)

    @Query("SELECT * FROM sectores ORDER BY id DESC")
    fun obtenerSectores(): Flow<List<Sector>>

    @Query("SELECT COUNT(*) FROM sectores WHERE LOWER(codigo) = LOWER(:codigo)")
    suspend fun contarPorCodigo(codigo: String): Int

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Sector?

    // Igual que contarPorCodigo, pero ignora el sector que se está editando
    @Query("SELECT COUNT(*) FROM sectores WHERE LOWER(codigo) = LOWER(:codigo) AND id != :idExcluido")
    suspend fun contarPorCodigoExcluyendo(codigo: String, idExcluido: Int): Int

    // Devuelve la cantidad de filas actualizadas (0 = no existe el sector)
    @Query(
        "UPDATE sectores SET codigo = :codigo, nombre = :nombre, ubicacion = :ubicacion, " +
                "estado = :estado, fotoUri = :fotoUri WHERE id = :id"
    )
    suspend fun actualizar(
        id: Int,
        codigo: String,
        nombre: String,
        ubicacion: String,
        estado: String,
        fotoUri: String?
    ): Int

    // --- Espejo del servidor (la app guarda en Room lo que descarga) ---
    @Insert
    suspend fun insertarTodos(lista: List<Sector>)

    @Query("DELETE FROM sectores")
    suspend fun eliminarTodos()

    @Query("DELETE FROM sectores WHERE id = :id")
    suspend fun eliminar(id: Int)
}
