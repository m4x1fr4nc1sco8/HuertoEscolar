package cl.duoc.huertoescolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.huertoescolar.model.Sector
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {

    @Insert
    suspend fun insertar(sector: Sector)

    @Query("SELECT * FROM sectores ORDER BY id DESC")
    fun obtenerSectores(): Flow<List<Sector>>
}