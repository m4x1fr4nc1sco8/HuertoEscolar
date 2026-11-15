package cl.duoc.huertoescolar.data.remote

data class SectorDto(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val estado: String,
    val fechaCreacion: String
)