package cl.duoc.huertoescolar.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints del microservicio huerto-api (Spring Boot + MySQL)
interface HuertoApi {

    // ---------- Sectores ----------
    @GET("sectores")
    suspend fun obtenerSectores(): List<SectorDto>

    @POST("sectores")
    suspend fun crearSector(@Body sector: SectorSolicitud): SectorDto

    @PUT("sectores/{id}")
    suspend fun actualizarSector(
        @Path("id") id: Int,
        @Body sector: SectorSolicitud
    ): SectorDto

    @DELETE("sectores/{id}")
    suspend fun eliminarSector(@Path("id") id: Int)

    // ---------- Cultivos ----------
    @GET("cultivos")
    suspend fun obtenerCultivos(): List<CultivoDto>

    @POST("cultivos")
    suspend fun crearCultivo(@Body cultivo: CultivoSolicitud): CultivoDto

    @DELETE("cultivos/{id}")
    suspend fun eliminarCultivo(@Path("id") id: Int)

    @PATCH("cultivos/{id}/estado")
    suspend fun cambiarEstadoCultivo(
        @Path("id") id: Int,
        @Body estado: EstadoSolicitud
    ): CultivoDto

    @POST("cultivos/{id}/tarea/enviar")
    suspend fun enviarTarea(
        @Path("id") id: Int,
        @Body tarea: EnviarTareaSolicitud
    ): CultivoDto

    @POST("cultivos/{id}/tarea/validar")
    suspend fun validarTarea(@Path("id") id: Int): CultivoDto

    @POST("cultivos/{id}/tarea/rechazar")
    suspend fun rechazarTarea(
        @Path("id") id: Int,
        @Body rechazo: RechazarTareaSolicitud
    ): CultivoDto

    // ---------- Usuarios ----------
    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioDto>

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: UsuarioSolicitud): UsuarioDto
}
