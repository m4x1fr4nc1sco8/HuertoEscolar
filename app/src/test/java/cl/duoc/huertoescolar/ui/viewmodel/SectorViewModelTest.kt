package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SectorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositorio: SectorRepository
    private lateinit var viewModel: SectorViewModel

    // Registro de las fotos que el ViewModel pidió copiar a almacenamiento permanente
    private val fotosGuardadas = mutableListOf<String>()
    private var rutaQueDevuelveGuardarFoto: String? = "file:///permanente/foto.jpg"

    @Before
    fun preparar() {
        repositorio = mockk()
        coEvery { repositorio.existeCodigo(any()) } returns false
        coEvery { repositorio.existeCodigoEnOtroSector(any(), any()) } returns false
        coEvery { repositorio.guardar(any()) } just Runs
        viewModel = SectorViewModel(
            repository = repositorio,
            guardarFoto = { temporal ->
                fotosGuardadas += temporal
                rutaQueDevuelveGuardarFoto
            },
            fechaHoy = { "18/11/2026" }
        )
    }

    private fun sectorExistente(estado: String = "EN_USO") = Sector(
        id = 7,
        codigo = "S7",
        nombre = "Invernadero",
        ubicacion = "Costado sur",
        estado = estado,
        fechaCreacion = "01/01/2026",
        fotoUri = "file:///permanente/sector7.jpg"
    )

    private fun llenarFormularioValido() {
        viewModel.cambiarCodigo("  S1  ")
        viewModel.cambiarNombre("  Huerta Norte  ")
        viewModel.cambiarUbicacion("  Patio central  ")
        viewModel.cambiarConfirma(true)
    }

    // ---------- campos del formulario ----------

    @Test
    fun cambiarCampos_actualizanElEstadoDelFormulario() {
        // ACT
        viewModel.cambiarCodigo("S1")
        viewModel.cambiarNombre("Huerta")
        viewModel.cambiarUbicacion("Patio")
        viewModel.cambiarEnUso(true)
        viewModel.cambiarEstado(EstadoSector.EN_MANTENIMIENTO)
        viewModel.cambiarConfirma(true)
        viewModel.cambiarFoto("content://foto/1")

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("S1", estado.codigo)
        assertEquals("Huerta", estado.nombre)
        assertEquals("Patio", estado.ubicacion)
        assertTrue(estado.enUso)
        assertEquals(EstadoSector.EN_MANTENIMIENTO, estado.estado)
        assertTrue(estado.confirma)
        assertEquals("content://foto/1", estado.fotoUri)
    }

    @Test
    fun cambiarCampos_conErroresPrevios_limpianSoloSuPropioError() {
        // ARRANGE: el formulario vacío provoca todos los errores
        viewModel.guardar()
        assertNotNull(viewModel.uiState.value.errorCodigo)
        assertNotNull(viewModel.uiState.value.errorNombre)
        assertNotNull(viewModel.uiState.value.errorUbicacion)
        assertNotNull(viewModel.uiState.value.errorConfirma)

        // ACT + ASSERT: cada cambio borra únicamente el error de su campo
        viewModel.cambiarCodigo("S1")
        assertNull(viewModel.uiState.value.errorCodigo)
        assertNotNull(viewModel.uiState.value.errorNombre)

        viewModel.cambiarNombre("Huerta")
        assertNull(viewModel.uiState.value.errorNombre)
        assertNotNull(viewModel.uiState.value.errorUbicacion)

        viewModel.cambiarUbicacion("Patio")
        assertNull(viewModel.uiState.value.errorUbicacion)
        assertNotNull(viewModel.uiState.value.errorConfirma)

        viewModel.cambiarConfirma(true)
        assertNull(viewModel.uiState.value.errorConfirma)
    }

    @Test
    fun quitarFoto_conFotoCargada_dejaLaFotoEnNull() {
        // ARRANGE
        viewModel.cambiarFoto("content://foto/1")

        // ACT
        viewModel.quitarFoto()

        // ASSERT
        assertNull(viewModel.uiState.value.fotoUri)
    }

    @Test
    fun limpiar_conElFormularioLleno_vuelveAlEstadoInicial() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarEnUso(true)

        // ACT
        viewModel.limpiar()

        // ASSERT
        assertEquals(SectorUiState(), viewModel.uiState.value)
    }

    // ---------- guardar: validaciones ----------

    @Test
    fun guardar_conFormularioVacio_marcaTodosLosErroresYNoGuarda() {
        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("El código es obligatorio", estado.errorCodigo)
        assertEquals("El nombre del sector es obligatorio", estado.errorNombre)
        assertEquals("La ubicación es obligatorio", estado.errorUbicacion)
        assertEquals("Debes confirmar los datos", estado.errorConfirma)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun guardar_conNombreMuyCorto_marcaErrorYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarNombre("ab")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(
            "El nombre del sector debe tener al menos 3 caracteres",
            viewModel.uiState.value.errorNombre
        )
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun guardar_sinConfirmarLosDatos_soloMarcaErrorDeConfirmacion() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarConfirma(false)

        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("Debes confirmar los datos", estado.errorConfirma)
        assertNull(estado.errorCodigo)
        assertNull(estado.errorNombre)
        assertNull(estado.errorUbicacion)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    @Test
    fun guardar_conCodigoYaExistente_marcaErrorDeCodigoYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        coEvery { repositorio.existeCodigo("S1") } returns true

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Ya existe un sector con ese código", viewModel.uiState.value.errorCodigo)
        coVerify(exactly = 0) { repositorio.guardar(any()) }
    }

    // ---------- guardar: crear un sector nuevo ----------

    @Test
    fun guardar_sectorNuevoValido_guardaConDatosLimpiosYEstadoDisponible() {
        // ARRANGE
        val guardado = slot<Sector>()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()

        // ACT
        viewModel.guardar()

        // ASSERT
        val sector = guardado.captured
        assertEquals("S1", sector.codigo)
        assertEquals("Huerta Norte", sector.nombre)
        assertEquals("Patio central", sector.ubicacion)
        assertEquals(EstadoSector.DISPONIBLE.name, sector.estado)
        assertEquals("18/11/2026", sector.fechaCreacion)
        assertNull(sector.fotoUri)
    }

    @Test
    fun guardar_sectorNuevoValido_avisaYReiniciaElFormulario() {
        // ARRANGE
        llenarFormularioValido()

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(
            SectorUiState(mensaje = "Sector guardado correctamente"),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { repositorio.guardar(any()) }
    }

    @Test
    fun guardar_sectorNuevoMarcadoEnUso_loGuardaConEstadoEnUso() {
        // ARRANGE
        val guardado = slot<Sector>()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()
        viewModel.cambiarEnUso(true)

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(EstadoSector.EN_USO.name, guardado.captured.estado)
    }

    @Test
    fun guardar_conFotoTemporal_laCopiaAAlmacenamientoPermanenteYGuardaLaNuevaRuta() {
        // ARRANGE
        val guardado = slot<Sector>()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()
        viewModel.cambiarFoto("content://temporal/foto.jpg")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(listOf("content://temporal/foto.jpg"), fotosGuardadas)
        assertEquals("file:///permanente/foto.jpg", guardado.captured.fotoUri)
    }

    @Test
    fun guardar_conFotoYaGuardada_laConservaSinVolverACopiar() {
        // ARRANGE
        val guardado = slot<Sector>()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()
        viewModel.cambiarFoto("file:///permanente/existente.jpg")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertTrue(fotosGuardadas.isEmpty())
        assertEquals("file:///permanente/existente.jpg", guardado.captured.fotoUri)
    }

    @Test
    fun guardar_cuandoNoSePuedeCopiarLaFoto_guardaElSectorSinFoto() {
        // ARRANGE
        rutaQueDevuelveGuardarFoto = null
        val guardado = slot<Sector>()
        coEvery { repositorio.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()
        viewModel.cambiarFoto("content://temporal/foto.jpg")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertNull(guardado.captured.fotoUri)
    }

    // ---------- cargar un sector para editarlo ----------

    @Test
    fun cargarParaEditar_sectorExistente_llenaElFormularioConSusDatos() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente()

        // ACT
        viewModel.cargarParaEditar(7)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals(7, estado.id)
        assertEquals("S7", estado.codigo)
        assertEquals("Invernadero", estado.nombre)
        assertEquals("Costado sur", estado.ubicacion)
        assertEquals(EstadoSector.EN_USO, estado.estado)
        assertEquals("file:///permanente/sector7.jpg", estado.fotoUri)
    }

    @Test
    fun cargarParaEditar_conEstadoDesconocido_usaDisponible() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente(estado = "RARO")

        // ACT
        viewModel.cargarParaEditar(7)

        // ASSERT
        assertEquals(EstadoSector.DISPONIBLE, viewModel.uiState.value.estado)
    }

    @Test
    fun cargarParaEditar_sectorInexistente_avisaQueNoSeEncontro() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(99) } returns null

        // ACT
        viewModel.cargarParaEditar(99)

        // ASSERT
        assertEquals("No se encontró el sector", viewModel.uiState.value.mensaje)
        assertNull(viewModel.uiState.value.id)
    }

    @Test
    fun cargarParaEditar_siElSectorYaEstaCargado_noVuelveAConsultarElRepositorio() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente()
        viewModel.cargarParaEditar(7)

        // ACT
        viewModel.cargarParaEditar(7)

        // ASSERT
        coVerify(exactly = 1) { repositorio.obtenerPorId(7) }
    }

    // ---------- guardar: editar un sector existente ----------

    @Test
    fun guardar_editandoUnSector_actualizaConElEstadoElegidoYAvisa() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente()
        coEvery { repositorio.actualizar(any(), any(), any(), any(), any(), any()) } returns true
        viewModel.cargarParaEditar(7)
        viewModel.cambiarNombre("  Invernadero 2  ")
        viewModel.cambiarEstado(EstadoSector.EN_MANTENIMIENTO)
        viewModel.cambiarConfirma(true)

        // ACT
        viewModel.guardar()

        // ASSERT
        coVerify(exactly = 1) {
            repositorio.actualizar(
                id = 7,
                codigo = "S7",
                nombre = "Invernadero 2",
                ubicacion = "Costado sur",
                estado = EstadoSector.EN_MANTENIMIENTO.name,
                fotoUri = "file:///permanente/sector7.jpg"
            )
        }
        coVerify(exactly = 0) { repositorio.guardar(any()) }
        val estado = viewModel.uiState.value
        assertEquals("Sector actualizado correctamente", estado.mensaje)
        assertTrue(estado.guardado)
    }

    @Test
    fun guardar_editandoYElRepositorioFalla_avisaQueNoSePudoActualizar() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente()
        coEvery { repositorio.actualizar(any(), any(), any(), any(), any(), any()) } returns false
        viewModel.cargarParaEditar(7)
        viewModel.cambiarConfirma(true)

        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("No se pudo actualizar el sector", estado.mensaje)
        assertFalse(estado.guardado)
    }

    @Test
    fun guardar_editandoConCodigoDeOtroSector_marcaErrorYNoActualiza() {
        // ARRANGE
        coEvery { repositorio.obtenerPorId(7) } returns sectorExistente()
        coEvery { repositorio.existeCodigoEnOtroSector("S7", 7) } returns true
        viewModel.cargarParaEditar(7)
        viewModel.cambiarConfirma(true)

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Ya existe un sector con ese código", viewModel.uiState.value.errorCodigo)
        coVerify(exactly = 0) { repositorio.actualizar(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { repositorio.existeCodigo(any()) }
    }
}
