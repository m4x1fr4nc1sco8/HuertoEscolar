package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.remote.RetrofitClient
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectoresApi

class SectoresRemoteRepository(
    private val api: SectoresApi = RetrofitClient.sectoresApi
) {
    suspend fun obtenerSectores(): List<SectorDto> {
        return api.obtenerSectores()
    }
}