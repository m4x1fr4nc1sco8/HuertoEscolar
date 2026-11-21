package cl.duoc.huertoescolar.data.remote

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.model.Usuario

// La app guarda las fechas como dd/MM/yyyy y el servidor usa yyyy-MM-dd.
// Se rellenan con ceros porque "1/3/2026" también es una fecha válida para la app.
fun fechaAIso(local: String): String {
    val partes = local.trim().split("/")
    if (partes.size != 3) return local.trim()
    return "${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}"
}

fun fechaALocal(iso: String): String {
    val partes = iso.trim().split("-")
    if (partes.size != 3) return iso.trim()
    return "${partes[2].padStart(2, '0')}/${partes[1].padStart(2, '0')}/${partes[0]}"
}

// ---------- Sector ----------
fun Sector.aSolicitud() = SectorSolicitud(
    codigo = codigo,
    nombre = nombre,
    ubicacion = ubicacion,
    estado = estado,
    fotoUrl = fotoUri
)

fun SectorDto.aSector() = Sector(
    id = id,
    codigo = codigo,
    nombre = nombre,
    ubicacion = ubicacion,
    estado = estado,
    fechaCreacion = fechaALocal(fechaCreacion),
    fotoUri = fotoUrl
)

// ---------- Cultivo ----------
fun Cultivo.aSolicitud() = CultivoSolicitud(
    nombre = nombre,
    sectorId = sectorId,
    estudianteId = estudianteId,
    fechaSiembra = fechaAIso(fechaSiembra),
    fechaCosecha = fechaAIso(fechaCosecha),
    fechaLimite = fechaLimite?.takeIf { it.isNotBlank() }?.let { fechaAIso(it) },
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = estado
)

fun CultivoDto.aCultivo() = Cultivo(
    id = id,
    nombre = nombre,
    sectorId = sectorId,
    fechaSiembra = fechaALocal(fechaSiembra),
    fechaCosecha = fechaALocal(fechaCosecha),
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = estado,
    estudianteId = estudianteId,
    estadoTarea = estadoTarea,
    fotoUri = fotoUrl,
    motivoRechazo = motivoRechazo,
    fechaLimite = fechaLimite?.let { fechaALocal(it) },
    comentarioEstudiante = comentarioEstudiante
)

// ---------- Usuario ----------
fun Usuario.aSolicitud() = UsuarioSolicitud(
    nombre = nombre,
    apellido = apellido,
    correo = correo,
    fechaNacimiento = fechaAIso(fechaNacimiento),
    clave = clave,
    rol = tipoUsuario
)

// El servidor no entrega la clave: se conserva la que ya había en el dispositivo
fun UsuarioDto.aUsuario(clave: String) = Usuario(
    id = id,
    nombre = nombre,
    apellido = apellido,
    correo = correo,
    fechaNacimiento = fechaALocal(fechaNacimiento),
    clave = clave,
    tipoUsuario = rol
)
