package cl.duoc.huertoescolar.data.remote

// Modelos que viajan por la red (JSON) hacia y desde el microservicio.
// "Solicitud" = lo que la app envía, "Dto" = lo que el servidor responde.
// Las fechas viajan como yyyy-MM-dd.

data class SectorSolicitud(
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val estado: String,
    val fotoUrl: String?
)

data class CultivoSolicitud(
    val nombre: String,
    val sectorId: Int,
    val estudianteId: Int?,
    val fechaSiembra: String,
    val fechaCosecha: String,
    val fechaLimite: String?,
    val frecuenciaRiegoDias: Int,
    val estado: String
)

data class CultivoDto(
    val id: Int,
    val nombre: String,
    val sectorId: Int,
    val estudianteId: Int?,
    val fechaSiembra: String,
    val fechaCosecha: String,
    val fechaLimite: String?,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val estadoTarea: String,
    val fotoUrl: String?,
    val comentarioEstudiante: String?,
    val motivoRechazo: String?
)

data class EstadoSolicitud(val estado: String)

data class EnviarTareaSolicitud(
    val estudianteId: Int,
    val comentario: String,
    val fotoUrl: String?
)

data class RechazarTareaSolicitud(val motivo: String)

data class UsuarioSolicitud(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val fechaNacimiento: String,
    val clave: String,
    val rol: String
)

// El servidor nunca devuelve la clave
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val fechaNacimiento: String,
    val rol: String
)

// Cuerpo de los errores del servidor: { "mensaje": "..." }
data class ErrorApi(val mensaje: String? = null)
