package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.SectorDao
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.SectorSolicitud
import cl.duoc.huertoescolar.data.remote.aSector
import cl.duoc.huertoescolar.data.remote.aSolicitud
import cl.duoc.huertoescolar.domain.model.Sector
import kotlinx.coroutines.flow.Flow

// El servidor (MySQL) es la fuente de los datos; Room guarda una copia para
// mostrar las listas y para seguir funcionando si no hay conexión.
class SectorRepository(
    private val sectorDao: SectorDao,
    private val api: HuertoApi
) {
    // Crea el sector en el servidor y guarda la copia con el id que le asignó
    suspend fun guardar(sector: Sector) {
        val creado = api.crearSector(sector.aSolicitud())
        sectorDao.insertar(creado.aSector())
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
        api.actualizarSector(id, SectorSolicitud(codigo, nombre, ubicacion, estado, fotoUri))
        return sectorDao.actualizar(id, codigo, nombre, ubicacion, estado, fotoUri) > 0
    }

    // El servidor rechaza (409) si el sector todavía tiene cultivos
    suspend fun eliminar(id: Int) {
        api.eliminarSector(id)
        sectorDao.eliminar(id)
    }

    // Descarga los sectores del servidor y reemplaza la copia local.
    // Si la descarga falla, la copia local no se toca.
    suspend fun sincronizar() {
        val remotos = api.obtenerSectores()
        sectorDao.eliminarTodos()
        sectorDao.insertarTodos(remotos.map { it.aSector() })
    }
}
