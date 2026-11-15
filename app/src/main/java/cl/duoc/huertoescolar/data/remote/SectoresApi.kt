package cl.duoc.huertoescolar.data.remote

import retrofit2.http.GET

interface SectoresApi {

    @GET("sectores")
    suspend fun obtenerSectores(): List<SectorDto>
}