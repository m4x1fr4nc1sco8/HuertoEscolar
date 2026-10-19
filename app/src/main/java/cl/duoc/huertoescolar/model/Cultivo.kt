package cl.duoc.huertoescolar.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cultivos")
data class Cultivo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val sectorId: Int,
    val fechaSiembra: String,
    val fechaCosecha: String,
    val frecuenciaRiegoDias: Int,
    val estado: String
)