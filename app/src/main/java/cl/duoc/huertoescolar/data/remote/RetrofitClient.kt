package cl.duoc.huertoescolar.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 es el computador que ejecuta el emulador (donde corre huerto-api)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val sectoresApi: SectoresApi by lazy { retrofit.create(SectoresApi::class.java) }

    val huertoApi: HuertoApi by lazy { retrofit.create(HuertoApi::class.java) }
}
