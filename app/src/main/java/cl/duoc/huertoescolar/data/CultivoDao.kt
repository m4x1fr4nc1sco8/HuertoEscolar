package cl.duoc.huertoescolar.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.huertoescolar.model.Cultivo
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {

    @Insert
    suspend fun insertar(cultivo: Cultivo)

    @Query("SELECT * FROM cultivos ORDER BY id DESC")
    fun obtenerCultivos(): Flow<List<Cultivo>>

    // Cultivos asignados a un estudiante
    @Query("SELECT * FROM cultivos WHERE estudianteId = :estudianteId ORDER BY id DESC")
    fun obtenerPorEstudiante(estudianteId: Int): Flow<List<Cultivo>>

    // Cultivos en un estado de tarea concreto (ej: pendientes de validación)
    @Query("SELECT * FROM cultivos WHERE estadoTarea = :estadoTarea ORDER BY id DESC")
    fun obtenerPorEstadoTarea(estadoTarea: String): Flow<List<Cultivo>>

    // El estudiante envía la foto. Solo afecta si el cultivo es suyo.
    // Devuelve la cantidad de filas actualizadas (0 = no se pudo).
    @Query(
        "UPDATE cultivos SET estadoTarea = :nuevoEstado, fotoUri = :fotoUri, " +
                "motivoRechazo = NULL WHERE id = :id AND estudianteId = :estudianteId"
    )
    suspend fun enviarARevision(
        id: Int,
        estudianteId: Int,
        fotoUri: String,
        nuevoEstado: String
    ): Int

    // El admin valida o rechaza. Solo afecta si el cultivo está en el estado esperado.
    @Query(
        "UPDATE cultivos SET estadoTarea = :nuevoEstado, motivoRechazo = :motivo " +
                "WHERE id = :id AND estadoTarea = :estadoEsperado"
    )
    suspend fun resolverRevision(
        id: Int,
        estadoEsperado: String,
        nuevoEstado: String,
        motivo: String?
    ): Int
}