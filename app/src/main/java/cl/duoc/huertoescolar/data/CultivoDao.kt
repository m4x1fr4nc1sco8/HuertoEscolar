package cl.duoc.huertoescolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.huertoescolar.model.Cultivo
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {

    @Insert
    suspend fun insertar(cultivo: Cultivo)

    @Query("SELECT * FROM cultivos ORDER BY id DESC")
    fun obtenerCultivos(): Flow<List<Cultivo>>
}