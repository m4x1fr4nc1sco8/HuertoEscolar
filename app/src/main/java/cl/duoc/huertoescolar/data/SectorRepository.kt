package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.model.Sector
import kotlinx.coroutines.flow.Flow

class SectorRepository(
    private val sectorDao: SectorDao
) {
    suspend fun guardar(sector: Sector) {
        sectorDao.insertar(sector)
    }

    fun obtenerSectores(): Flow<List<Sector>> {
        return sectorDao.obtenerSectores()
    }

    suspend fun existeCodigo(codigo: String): Boolean {
        return sectorDao.contarPorCodigo(codigo) > 0
    }
}