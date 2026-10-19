package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.model.Cultivo
import kotlinx.coroutines.flow.Flow

class CultivoRepository(
    private val cultivoDao: CultivoDao
) {
    suspend fun guardar(cultivo: Cultivo) {
        cultivoDao.insertar(cultivo)
    }

    fun obtenerCultivos(): Flow<List<Cultivo>> {
        return cultivoDao.obtenerCultivos()
    }
}