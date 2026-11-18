package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.SectorDao
import cl.duoc.huertoescolar.domain.model.Sector
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

    suspend fun obtenerPorId(id: Int): Sector? {
        return sectorDao.obtenerPorId(id)
    }

    suspend fun existeCodigoEnOtroSector(codigo: String, idActual: Int): Boolean {
        return sectorDao.contarPorCodigoExcluyendo(codigo, idActual) > 0
    }

    suspend fun actualizar(
        id: Int,
        codigo: String,
        nombre: String,
        ubicacion: String,
        estado: String,
        fotoUri: String?
    ): Boolean {
        return sectorDao.actualizar(id, codigo, nombre, ubicacion, estado, fotoUri) > 0
    }
}