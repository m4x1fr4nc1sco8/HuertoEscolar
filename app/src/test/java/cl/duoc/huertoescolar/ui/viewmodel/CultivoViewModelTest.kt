package cl.duoc.huertoescolar.ui.viewmodel

import cl.duoc.huertoescolar.MainDispatcherRule
import cl.duoc.huertoescolar.data.repository.CultivoRepository
import cl.duoc.huertoescolar.data.repository.SectorRepository
import cl.duoc.huertoescolar.data.repository.UsuarioRepository
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.model.Usuario
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CultivoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var cultivoRepo: CultivoRepository
    private lateinit var sectorRepo: SectorRepository
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var viewModel: CultivoViewModel

    private val sector = Sector(
        id = 1,
        codigo = "S1",
        nombre = "Huerta Norte",
        ubicacion = "Patio",
        estado = "DISPONIBLE",
        fechaCreacion = "01/01/2026"
    )

    private val estudiante = Usuario(
        id = 5,
        nombre = "Ana",
        apellido = "Soto",
        correo = "ana@huerto.cl",
        fechaNacimiento = "01/01/2012",
        clave = "12345678",
        tipoUsuario = Rol.ESTUDIANTE.name
    )

    @Before
    fun preparar() {
        cultivoRepo = mockk()
        sectorRepo = mockk()
        usuarioRepo = mockk()
        every { sectorRepo.obtenerSectores() } returns flowOf(listOf(sector))
        every { usuarioRepo.obtenerEstudiantes() } returns flowOf(listOf(estudiante))
        coEvery { cultivoRepo.guardar(any()) } just Runs
        viewModel = CultivoViewModel(cultivoRepo, sectorRepo, usuarioRepo)
    }

    private fun llenarFormularioValido() {
        viewModel.cambiarNombre("  Tomates  ")
        viewModel.seleccionarSector(sector)
        viewModel.seleccionarEstudiante(estudiante)
        viewModel.cambiarSiembra("01/03/2026")
        viewModel.cambiarCosecha("01/06/2026")
        viewModel.cambiarLimite("15/05/2026")
        viewModel.cambiarRiego("3")
    }

    // ---------- listas para los selectores ----------

    @Test
    fun sectoresYEstudiantes_sinSuscriptor_empiezanVacios() {
        assertEquals(emptyList<Sector>(), viewModel.sectores.value)
        assertEquals(emptyList<Usuario>(), viewModel.estudiantes.value)
    }

    @Test
    fun sectoresYEstudiantes_conSuscriptor_emitenLosDatosDeLosRepositorios() = runTest {
        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sectores.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.estudiantes.collect {}
        }

        // ASSERT
        assertEquals(listOf(sector), viewModel.sectores.value)
        assertEquals(listOf(estudiante), viewModel.estudiantes.value)
    }

    // ---------- campos del formulario ----------

    @Test
    fun seleccionarSector_conUnSector_guardaIdYTextoParaMostrar() {
        // ACT
        viewModel.seleccionarSector(sector)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals(1, estado.sectorId)
        assertEquals("S1 · Huerta Norte", estado.sectorTexto)
    }

    @Test
    fun seleccionarEstudiante_conUnEstudiante_guardaIdYNombreCompleto() {
        // ACT
        viewModel.seleccionarEstudiante(estudiante)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals(5, estado.estudianteId)
        assertEquals("Ana Soto", estado.estudianteTexto)
    }

    @Test
    fun cambiarCampos_actualizanElEstadoDelFormulario() {
        // ACT
        viewModel.cambiarNombre("Lechuga")
        viewModel.cambiarSiembra("02/03/2026")
        viewModel.cambiarCosecha("02/05/2026")
        viewModel.cambiarLimite("01/05/2026")
        viewModel.cambiarRiego("2")
        viewModel.cambiarEstado(EstadoCultivo.SEMBRADO)

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("Lechuga", estado.nombre)
        assertEquals("02/03/2026", estado.fechaSiembra)
        assertEquals("02/05/2026", estado.fechaCosecha)
        assertEquals("01/05/2026", estado.fechaLimite)
        assertEquals("2", estado.frecuenciaRiego)
        assertEquals(EstadoCultivo.SEMBRADO, estado.estado)
    }

    @Test
    fun cambiarCampos_conErroresPrevios_limpianSoloSuPropioError() {
        // ARRANGE: el formulario vacío provoca todos los errores
        viewModel.guardar()
        val conErrores = viewModel.uiState.value
        assertNotNull(conErrores.errorNombre)
        assertNotNull(conErrores.errorLimite)
        assertNotNull(conErrores.errorRiego)

        // ACT
        viewModel.cambiarNombre("Tomates")
        viewModel.cambiarLimite("15/05/2026")
        viewModel.cambiarRiego("3")

        // ASSERT
        val estado = viewModel.uiState.value
        assertNull(estado.errorNombre)
        assertNull(estado.errorLimite)
        assertNull(estado.errorRiego)
        // los demás errores siguen hasta que se corrija ese campo
        assertNotNull(estado.errorSector)
        assertNotNull(estado.errorEstudiante)
    }

    @Test
    fun cambiarSiembra_conErroresPrevios_limpiaErrorDeSiembraYDeCosecha() {
        // ARRANGE
        viewModel.guardar()
        assertNotNull(viewModel.uiState.value.errorSiembra)
        assertNotNull(viewModel.uiState.value.errorCosecha)

        // ACT
        viewModel.cambiarSiembra("01/03/2026")

        // ASSERT
        assertNull(viewModel.uiState.value.errorSiembra)
        assertNull(viewModel.uiState.value.errorCosecha)
    }

    @Test
    fun cambiarCosecha_conErrorPrevio_limpiaErrorDeCosecha() {
        // ARRANGE
        viewModel.guardar()
        assertNotNull(viewModel.uiState.value.errorCosecha)

        // ACT
        viewModel.cambiarCosecha("01/06/2026")

        // ASSERT
        assertNull(viewModel.uiState.value.errorCosecha)
    }

    @Test
    fun seleccionarSectorYEstudiante_conErroresPrevios_losLimpian() {
        // ARRANGE
        viewModel.guardar()
        assertEquals("Selecciona un sector", viewModel.uiState.value.errorSector)
        assertEquals("Selecciona un estudiante", viewModel.uiState.value.errorEstudiante)

        // ACT
        viewModel.seleccionarSector(sector)
        viewModel.seleccionarEstudiante(estudiante)

        // ASSERT
        assertNull(viewModel.uiState.value.errorSector)
        assertNull(viewModel.uiState.value.errorEstudiante)
    }

    @Test
    fun limpiar_conElFormularioLleno_vuelveAlEstadoInicial() {
        // ARRANGE
        llenarFormularioValido()

        // ACT
        viewModel.limpiar()

        // ASSERT
        assertEquals(CultivoUiState(), viewModel.uiState.value)
    }

    // ---------- guardar: validaciones ----------

    @Test
    fun guardar_conFormularioVacio_marcaTodosLosErroresYNoGuarda() {
        // ACT
        viewModel.guardar()

        // ASSERT
        val estado = viewModel.uiState.value
        assertEquals("El nombre del cultivo es obligatorio", estado.errorNombre)
        assertEquals("Selecciona un sector", estado.errorSector)
        assertEquals("Selecciona un estudiante", estado.errorEstudiante)
        assertEquals("La fecha de siembra es obligatoria", estado.errorSiembra)
        assertEquals("La fecha de cosecha es obligatoria", estado.errorCosecha)
        assertEquals("La fecha límite es obligatoria", estado.errorLimite)
        assertEquals("La frecuencia es obligatoria", estado.errorRiego)
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    @Test
    fun guardar_sinSectorSeleccionado_marcaErrorYNoGuarda() {
        // ARRANGE: todo completo excepto el sector
        viewModel.cambiarNombre("Tomates")
        viewModel.seleccionarEstudiante(estudiante)
        viewModel.cambiarSiembra("01/03/2026")
        viewModel.cambiarCosecha("01/06/2026")
        viewModel.cambiarLimite("15/05/2026")
        viewModel.cambiarRiego("3")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Selecciona un sector", viewModel.uiState.value.errorSector)
        assertNull(viewModel.uiState.value.errorEstudiante)
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    @Test
    fun guardar_sinEstudianteSeleccionado_marcaErrorYNoGuarda() {
        // ARRANGE
        viewModel.cambiarNombre("Tomates")
        viewModel.seleccionarSector(sector)
        viewModel.cambiarSiembra("01/03/2026")
        viewModel.cambiarCosecha("01/06/2026")
        viewModel.cambiarLimite("15/05/2026")
        viewModel.cambiarRiego("3")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Selecciona un estudiante", viewModel.uiState.value.errorEstudiante)
        assertNull(viewModel.uiState.value.errorSector)
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    @Test
    fun guardar_cosechaAnteriorALaSiembra_marcaErrorYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarCosecha("01/01/2026")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(
            "La cosecha debe ser posterior a la siembra",
            viewModel.uiState.value.errorCosecha
        )
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    @Test
    fun guardar_conFrecuenciaDeRiegoInvalida_marcaErrorYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarRiego("abc")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Ingresa solo números", viewModel.uiState.value.errorRiego)
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    @Test
    fun guardar_conFechaLimiteConFormatoIncorrecto_marcaErrorYNoGuarda() {
        // ARRANGE
        llenarFormularioValido()
        viewModel.cambiarLimite("2026-05-15")

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals("Usa el formato dd/mm/aaaa", viewModel.uiState.value.errorLimite)
        coVerify(exactly = 0) { cultivoRepo.guardar(any()) }
    }

    // ---------- guardar: caso feliz ----------

    @Test
    fun guardar_conFormularioValido_guardaElCultivoConLosDatosLimpios() {
        // ARRANGE
        val guardado = slot<Cultivo>()
        coEvery { cultivoRepo.guardar(capture(guardado)) } just Runs
        llenarFormularioValido()
        viewModel.cambiarEstado(EstadoCultivo.SEMBRADO)

        // ACT
        viewModel.guardar()

        // ASSERT
        val cultivo = guardado.captured
        assertEquals("Tomates", cultivo.nombre)
        assertEquals(1, cultivo.sectorId)
        assertEquals(5, cultivo.estudianteId)
        assertEquals("01/03/2026", cultivo.fechaSiembra)
        assertEquals("01/06/2026", cultivo.fechaCosecha)
        assertEquals("15/05/2026", cultivo.fechaLimite)
        assertEquals(3, cultivo.frecuenciaRiegoDias)
        assertEquals(EstadoCultivo.SEMBRADO.name, cultivo.estado)
    }

    @Test
    fun guardar_conFormularioValido_avisaYReiniciaElFormulario() {
        // ARRANGE
        llenarFormularioValido()

        // ACT
        viewModel.guardar()

        // ASSERT
        assertEquals(
            CultivoUiState(mensaje = "Cultivo guardado correctamente"),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { cultivoRepo.guardar(any()) }
    }
}
