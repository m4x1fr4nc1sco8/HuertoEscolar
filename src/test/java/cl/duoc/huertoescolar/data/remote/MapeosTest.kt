package cl.duoc.huertoescolar.data.remote

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeosTest {

    // ---------- fechas ----------

    @Test
    fun fechaAIso_fechaDeLaApp_retornaFormatoDelServidor() {
        assertEquals("2026-03-01", fechaAIso("01/03/2026"))
    }

    @Test
    fun fechaAIso_diaYMesSinCero_losRellenaConCeros() {
        assertEquals("2026-03-01", fechaAIso("1/3/2026"))
    }

    @Test
    fun fechaAIso_formatoInesperado_laDevuelveSinCambios() {
        assertEquals("2026-03-01", fechaAIso(" 2026-03-01 "))
    }

    @Test
    fun fechaALocal_fechaDelServidor_retornaFormatoDeLaApp() {
        assertEquals("17/11/2026", fechaALocal("2026-11-17"))
    }

    @Test
    fun fechaALocal_diaYMesSinCero_losRellenaConCeros() {
        assertEquals("07/03/2026", fechaALocal("2026-3-7"))
    }

    @Test
    fun fechaALocal_formatoInesperado_laDevuelveSinCambios() {
        assertEquals("17/11/2026", fechaALocal("17/11/2026"))
    }

    // ---------- sector ----------

    @Test
    fun sector_aSolicitud_copiaLosCamposQueEnviaLaApp() {
        // ARRANGE
        val sector = Sector(1, "S1", "Huerta", "Patio", "EN_USO", "01/03/2026", "file:///foto.jpg")

        // ACT
        val solicitud = sector.aSolicitud()

        // ASSERT
        assertEquals(SectorSolicitud("S1", "Huerta", "Patio", "EN_USO", "file:///foto.jpg"), solicitud)
    }

    @Test
    fun sectorDto_aSector_conviertePasaLaFechaYLaFoto() {
        // ARRANGE
        val dto = SectorDto(4, "S9", "Prueba", "Patio", "DISPONIBLE", "2026-11-20", "file:///f.jpg")

        // ACT
        val sector = dto.aSector()

        // ASSERT
        assertEquals(
            Sector(4, "S9", "Prueba", "Patio", "DISPONIBLE", "20/11/2026", "file:///f.jpg"),
            sector
        )
    }

    @Test
    fun sectorDto_sinFoto_dejaLaFotoEnNull() {
        // ARRANGE: el DTO del ejemplo antiguo (6 campos) no trae foto
        val dto = SectorDto(1, "S1", "Huerta", "Patio", "EN_USO", "2026-03-01")

        // ASSERT
        assertNull(dto.aSector().fotoUri)
    }

    // ---------- cultivo ----------

    @Test
    fun cultivo_aSolicitud_conFechasYFechaLimite_lasConvierteAlFormatoDelServidor() {
        // ARRANGE
        val cultivo = Cultivo(
            nombre = "Tomates", sectorId = 1, fechaSiembra = "01/03/2026",
            fechaCosecha = "1/6/2026", frecuenciaRiegoDias = 3, estado = "SEMBRADO",
            estudianteId = 2, fechaLimite = "15/05/2026"
        )

        // ACT
        val solicitud = cultivo.aSolicitud()

        // ASSERT
        assertEquals(
            CultivoSolicitud("Tomates", 1, 2, "2026-03-01", "2026-06-01", "2026-05-15", 3, "SEMBRADO"),
            solicitud
        )
    }

    @Test
    fun cultivo_aSolicitud_sinFechaLimiteOEnBlanco_laEnviaComoNull() {
        // ARRANGE
        val base = Cultivo(
            nombre = "Lechuga", sectorId = 1, fechaSiembra = "01/03/2026",
            fechaCosecha = "01/06/2026", frecuenciaRiegoDias = 2, estado = "PLANIFICADO"
        )

        // ASSERT
        assertNull(base.aSolicitud().fechaLimite)
        assertNull(base.copy(fechaLimite = "  ").aSolicitud().fechaLimite)
    }

    @Test
    fun cultivoDto_aCultivo_convierteFechasYCopiaElFlujoDeTarea() {
        // ARRANGE
        val dto = CultivoDto(
            id = 9, nombre = "Tomates", sectorId = 1, estudianteId = 2,
            fechaSiembra = "2026-03-01", fechaCosecha = "2026-06-01", fechaLimite = "2026-05-15",
            frecuenciaRiegoDias = 3, estado = "SEMBRADO", estadoTarea = "PENDIENTE_DE_VALIDACION",
            fotoUrl = "file:///t.jpg", comentarioEstudiante = "Listo", motivoRechazo = null
        )

        // ACT
        val cultivo = dto.aCultivo()

        // ASSERT
        assertEquals(
            Cultivo(
                id = 9, nombre = "Tomates", sectorId = 1, fechaSiembra = "01/03/2026",
                fechaCosecha = "01/06/2026", frecuenciaRiegoDias = 3, estado = "SEMBRADO",
                estudianteId = 2, estadoTarea = "PENDIENTE_DE_VALIDACION", fotoUri = "file:///t.jpg",
                motivoRechazo = null, fechaLimite = "15/05/2026", comentarioEstudiante = "Listo"
            ),
            cultivo
        )
    }

    @Test
    fun cultivoDto_sinFechaLimiteNiEstudiante_losDejaEnNull() {
        // ARRANGE
        val dto = CultivoDto(
            1, "Albahaca", 2, null, "2026-11-10", "2027-01-30", null,
            4, "PLANIFICADO", "PENDIENTE", null, null, null
        )

        // ACT
        val cultivo = dto.aCultivo()

        // ASSERT
        assertNull(cultivo.fechaLimite)
        assertNull(cultivo.estudianteId)
    }

    // ---------- usuario ----------

    @Test
    fun usuario_aSolicitud_convierteLaFechaDeNacimientoYEnviaElRol() {
        // ARRANGE
        val usuario = Usuario(0, "Ana", "Soto", "ana@huerto.cl", "05/07/2014", "12345678", "ESTUDIANTE")

        // ACT
        val solicitud = usuario.aSolicitud()

        // ASSERT
        assertEquals(
            UsuarioSolicitud("Ana", "Soto", "ana@huerto.cl", "2014-07-05", "12345678", "ESTUDIANTE"),
            solicitud
        )
    }

    @Test
    fun usuarioDto_aUsuario_usaLaClaveQueSeLeIndica() {
        // ARRANGE
        val dto = UsuarioDto(5, "Ana", "Soto", "ana@huerto.cl", "2014-07-05", "ESTUDIANTE")

        // ACT
        val usuario = dto.aUsuario(clave = "12345678")

        // ASSERT
        assertEquals(
            Usuario(5, "Ana", "Soto", "ana@huerto.cl", "05/07/2014", "12345678", "ESTUDIANTE"),
            usuario
        )
    }
}
