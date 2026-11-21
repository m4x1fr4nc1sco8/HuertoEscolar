package cl.duoc.huertoescolar.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

// Convierte un error de red o del servidor en un texto para mostrar al usuario
fun mensajeDeError(error: Throwable): String = when (error) {
    is HttpException ->
        mensajeDelServidor(error) ?: "El servidor respondió con un error (${error.code()})."
    is IOException ->
        "No se pudo conectar con el servidor. Comprueba la conexión."
    else ->
        "Ocurrió un error inesperado."
}

// El microservicio responde los errores como { "mensaje": "..." }
private fun mensajeDelServidor(error: HttpException): String? =
    try {
        val cuerpo = error.response()?.errorBody()?.string()
        Gson().fromJson(cuerpo, ErrorApi::class.java)?.mensaje?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
