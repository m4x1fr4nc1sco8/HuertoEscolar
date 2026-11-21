package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.local.CultivoDao
import cl.duoc.huertoescolar.data.remote.EnviarTareaSolicitud
import cl.duoc.huertoescolar.data.remote.EstadoSolicitud
import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.RechazarTareaSolicitud
import cl.duoc.huertoescolar.data.remote.aCultivo
import cl.duoc.huertoescolar.data.remote.aSolicitud
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import kotlinx.coroutines.flow.Flow

// Cada cambio se hace primero en el servidor y después se refleja en Room.
// Si el servidor falla, se lanza la excepción y Room no se modifica.
class CultivoRepository(
    private val cultivoDao: CultivoDao,
    private val api: HuertoApi
) {
    suspend fun guardar(cultivo: Cultivo) {
        val creado = api.crearCultivo(cultivo.aSolicitud())
        cultivoDao.insertar(creado.aCultivo())
    }

    fun obtenerCultivos(): Flow<List<Cultivo>> {
        return cultivoDao.obtenerCultivos()
    }

    // Admin: actualiza el estado de desarrollo del cultivo.
    suspend fun cambiarEstado(cultivoId: Int, estado: EstadoCultivo): Boolean {
        api.cambiarEstadoCultivo(cultivoId, EstadoSolicitud(estado.name))
        return cultivoDao.actualizarEstado(cultivoId, estado.name) > 0
    }

    suspend fun eliminar(cultivoId: Int) {
        api.eliminarCultivo(cultivoId)
        cultivoDao.eliminar(cultivoId)
    }

    // --- Flujo de tarea ---

    fun obtenerPorEstudiante(estudianteId: Int): Flow<List<Cultivo>> {
        return cultivoDao.obtenerPorEstudiante(estudianteId)
    }

    fun obtenerPendientesDeValidacion(): Flow<List<Cultivo>> {
        return cultivoDao.obtenerPorEstadoTarea(EstadoTarea.PENDIENTE_DE_VALIDACION.name)
    }

    // Estudiante: marca como realizada adjuntando la foto y un comentario. true si se registró.
    suspend fun marcarComoRealizada(
        cultivoId: Int,
        estudianteId: Int,
        fotoUri: String,
        comentario: String
    ): Boolean {
        val texto = comentario.trim()
        api.enviarTarea(cultivoId, EnviarTareaSolicitud(estudianteId, texto, fotoUri))
        return cultivoDao.enviarARevision(
            id = cultivoId,
            estudianteId = estudianteId,
            fotoUri = fotoUri,
            comentario = texto,
            nuevoEstado = EstadoTarea.PENDIENTE_DE_VALIDACION.name
        ) > 0
    }

    // Admin: valida la tarea enviada.
    suspend fun validar(cultivoId: Int): Boolean {
        api.validarTarea(cultivoId)
        return cultivoDao.resolverRevision(
            id = cultivoId,
            estadoEsperado = EstadoTarea.PENDIENTE_DE_VALIDACION.name,
            nuevoEstado = EstadoTarea.VALIDADA.name,
            motivo = null
        ) > 0
    }

    // Admin: rechaza; la tarea vuelve a PENDIENTE para que el estudiante reenvíe.
    suspend fun rechazar(cultivoId: Int, motivo: String): Boolean {
        val texto = motivo.trim()
        api.rechazarTarea(cultivoId, RechazarTareaSolicitud(texto))
        return cultivoDao.resolverRevision(
            id = cultivoId,
            estadoEsperado = EstadoTarea.PENDIENTE_DE_VALIDACION.name,
            nuevoEstado = EstadoTarea.PENDIENTE.name,
            motivo = texto
        ) > 0
    }

    // Descarga los cultivos del servidor y reemplaza la copia local.
    suspend fun sincronizar() {
        val remotos = api.obtenerCultivos()
        cultivoDao.eliminarTodos()
        cultivoDao.insertarTodos(remotos.map { it.aCultivo() })
    }
}
