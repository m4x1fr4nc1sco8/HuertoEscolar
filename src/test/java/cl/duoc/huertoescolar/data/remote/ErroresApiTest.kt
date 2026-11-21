package cl.duoc.huertoescolar.data.remote

import cl.duoc.huertoescolar.errorHttp
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErroresApiTest {

    private fun errorConCuerpo(codigo: Int, cuerpo: String): HttpException =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun mensajeDeError_servidorRespondeConMensaje_retornaElMensajeDelServidor() {
        // ACT
        val mensaje = mensajeDeError(errorHttp(409, "Ya existe un sector con ese código"))

        // ASSERT
        assertEquals("Ya existe un sector con ese código", mensaje)
    }

    @Test
    fun mensajeDeError_cuerpoQueNoEsJson_retornaMensajeConElCodigoHttp() {
        // ACT
        val mensaje = mensajeDeError(errorConCuerpo(500, "<html>Error</html>"))

        // ASSERT
        assertEquals("El servidor respondió con un error (500).", mensaje)
    }

    @Test
    fun mensajeDeError_cuerpoVacio_retornaMensajeConElCodigoHttp() {
        // ACT
        val mensaje = mensajeDeError(errorConCuerpo(404, ""))

        // ASSERT
        assertEquals("El servidor respondió con un error (404).", mensaje)
    }

    @Test
    fun mensajeDeError_mensajeEnBlanco_retornaMensajeConElCodigoHttp() {
        // ACT
        val mensaje = mensajeDeError(errorConCuerpo(400, """{"mensaje":"   "}"""))

        // ASSERT
        assertEquals("El servidor respondió con un error (400).", mensaje)
    }

    @Test
    fun mensajeDeError_problemaDeRed_avisaQueNoHayConexion() {
        // ACT
        val mensaje = mensajeDeError(IOException("Connection refused"))

        // ASSERT
        assertEquals("No se pudo conectar con el servidor. Comprueba la conexión.", mensaje)
    }

    @Test
    fun mensajeDeError_errorDesconocido_retornaMensajeGenerico() {
        // ACT
        val mensaje = mensajeDeError(IllegalStateException("raro"))

        // ASSERT
        assertEquals("Ocurrió un error inesperado.", mensaje)
    }
}
