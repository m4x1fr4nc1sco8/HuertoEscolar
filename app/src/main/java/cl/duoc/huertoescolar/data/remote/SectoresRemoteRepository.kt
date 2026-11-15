package cl.duoc.huertoescolar.data.remote

class SectoresRemoteRepository(
    private val api: SectoresApi = RetrofitClient.sectoresApi
) {
    suspend fun obtenerSectores(): List<SectorDto> {
        return api.obtenerSectores()
    }
}