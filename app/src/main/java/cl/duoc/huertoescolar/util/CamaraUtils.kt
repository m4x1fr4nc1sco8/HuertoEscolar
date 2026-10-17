package cl.duoc.huertoescolar.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriFotografia(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos")
    carpeta.mkdirs()
    val archivo = File.createTempFile("foto_", ".jpg", carpeta)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

fun guardarFotoPermanente(context: Context, origen: Uri): String? {
    return try {
        val carpeta = File(context.filesDir, "fotos_sectores")
        carpeta.mkdirs()
        val destino = File(carpeta, "sector_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        Uri.fromFile(destino).toString()
    } catch (e: Exception) {
        null
    }
}