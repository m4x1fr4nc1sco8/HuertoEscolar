package cl.duoc.huertoescolar.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// API externa pública y gratuita: https://open-meteo.com (no necesita clave)
interface ClimaApi {

    // Clima actual y pronóstico de 3 días. "daily" exige indicar la zona horaria.
    @GET(
        "v1/forecast?" +
                "current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&timezone=America/Santiago&forecast_days=3"
    )
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double
    ): ClimaRespuesta
}

data class ClimaRespuesta(
    @SerializedName("current") val actual: ClimaActualDto,
    @SerializedName("daily") val diario: ClimaDiarioDto
)

data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("relative_humidity_2m") val humedad: Int,
    @SerializedName("wind_speed_10m") val viento: Double,
    @SerializedName("weather_code") val codigo: Int
)

data class ClimaDiarioDto(
    @SerializedName("time") val fechas: List<String>,
    @SerializedName("weather_code") val codigos: List<Int>,
    @SerializedName("temperature_2m_max") val maximas: List<Double>,
    @SerializedName("temperature_2m_min") val minimas: List<Double>,
    @SerializedName("precipitation_probability_max") val probabilidadLluvia: List<Int?>
)
