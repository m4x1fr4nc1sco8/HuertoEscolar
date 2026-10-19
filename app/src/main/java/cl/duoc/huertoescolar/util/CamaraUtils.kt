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

// Copia la foto temporal a almacenamiento permanente y devuelve su Uri como texto.
// Los valores por defecto mantienen el comportamiento original (sectores).
fun guardarFotoPermanente(
    context: Context,
    origen: Uri,
    carpeta: String = "fotos_sectores",
    prefijo: String = "sector_"
): String? {
    return try {
        val dir = File(context.filesDir, carpeta)
        dir.mkdirs()
        val destino = File(dir, "${prefijo}${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        Uri.fromFile(destino).toString()
    } catch (e: Exception) {
        null
    }
}
