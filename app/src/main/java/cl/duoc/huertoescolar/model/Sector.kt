package cl.duoc.huertoescolar.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sectores")
data class Sector(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val estado: String,
    val fechaCreacion: String,
    val fotoUri: String? = null
)