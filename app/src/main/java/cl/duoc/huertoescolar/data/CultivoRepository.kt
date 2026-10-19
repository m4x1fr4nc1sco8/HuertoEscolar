package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.model.Cultivo
import cl.duoc.huertoescolar.model.EstadoTarea
import kotlinx.coroutines.flow.Flow

class CultivoRepository(
    private val cultivoDao: CultivoDao
) {
    suspend fun guardar(cultivo: Cultivo) {
        cultivoDao.insertar(cultivo)
    }

    fun obtenerCultivos(): Flow<List<Cultivo>> {
        return cultivoDao.obtenerCultivos()
    }

    // --- Flujo de tarea ---

    fun obtenerPorEstudiante(estudianteId: Int): Flow<List<Cultivo>> {
        return cultivoDao.obtenerPorEstudiante(estudianteId)
    }

    fun obtenerPendientesDeValidacion(): Flow<List<Cultivo>> {
        return cultivoDao.obtenerPorEstadoTarea(EstadoTarea.PENDIENTE_DE_VALIDACION.name)
    }

    // Estudiante: marca como realizada adjuntando la foto. true si se registró.
    suspend fun marcarComoRealizada(
        cultivoId: Int,
        estudianteId: Int,
        fotoUri: String
    ): Boolean {
        return cultivoDao.enviarARevision(
            id = cultivoId,
            estudianteId = estudianteId,
            fotoUri = fotoUri,
            nuevoEstado = EstadoTarea.PENDIENTE_DE_VALIDACION.name
        ) > 0
    }

    // Admin: valida la tarea enviada.
    suspend fun validar(cultivoId: Int): Boolean {
        return cultivoDao.resolverRevision(
            id = cultivoId,
            estadoEsperado = EstadoTarea.PENDIENTE_DE_VALIDACION.name,
            nuevoEstado = EstadoTarea.VALIDADA.name,
            motivo = null
        ) > 0
    }

    // Admin: rechaza; la tarea vuelve a PENDIENTE para que el estudiante reenvíe.
    suspend fun rechazar(cultivoId: Int, motivo: String): Boolean {
        return cultivoDao.resolverRevision(
            id = cultivoId,
            estadoEsperado = EstadoTarea.PENDIENTE_DE_VALIDACION.name,
            nuevoEstado = EstadoTarea.PENDIENTE.name,
            motivo = motivo.trim()
        ) > 0
    }
}