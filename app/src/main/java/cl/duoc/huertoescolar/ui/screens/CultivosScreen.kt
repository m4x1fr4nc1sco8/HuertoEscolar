package cl.duoc.huertoescolar.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.ui.components.CampoDesplegable
import cl.duoc.huertoescolar.util.crearUriFotografia
import cl.duoc.huertoescolar.util.estaAtrasada
import cl.duoc.huertoescolar.util.etiqueta
import cl.duoc.huertoescolar.util.guardarFotoPermanente
import cl.duoc.huertoescolar.ui.viewmodel.CultivosViewModel
import cl.duoc.huertoescolar.ui.viewmodel.Fabrica
// Si AsyncImage sale en rojo, usa el mismo import que tienes en SectoresScreen
// (coil3.compose.AsyncImage o coil.compose.AsyncImage).
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivosScreen(
    onVolver: () -> Unit,
    onNuevoCultivo: () -> Unit,
    puedeCrear: Boolean,
    viewModel: CultivosViewModel = viewModel(factory = Fabrica.cultivos)
) {
    val cultivos by viewModel.cultivos.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val usuario by SesionActual.usuario.collectAsStateWithLifecycle()
    val esEstudiante = usuario?.tipoUsuario == Rol.ESTUDIANTE.name
    val esAdministrador = usuario?.tipoUsuario == Rol.ADMINISTRADOR.name

    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Qué cultivo se está fotografiando y dónde quedó la foto temporal
    var cultivoEnCaptura by rememberSaveable { mutableStateOf<Int?>(null) }
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Cultivo cuyo comentario escribe el estudiante (abre el diálogo)
    var cultivoAComentar by rememberSaveable { mutableStateOf<Int?>(null) }
    // Comentario ya validado, a la espera de que se tome la foto
    var comentarioEnCaptura by rememberSaveable { mutableStateOf("") }

    // Cultivo que el admin está por rechazar (abre el diálogo del motivo)
    var cultivoARechazar by rememberSaveable { mutableStateOf<Int?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val id = cultivoEnCaptura
        val temporal = uriTemporal
        if (exito && id != null && temporal != null) {
            val rutaFinal = guardarFotoPermanente(
                context = contexto,
                origen = temporal,
                carpeta = "fotos_cultivos",
                prefijo = "cultivo_"
            )
            if (rutaFinal != null) {
                viewModel.marcarComoRealizada(id, rutaFinal, comentarioEnCaptura)
            } else {
                viewModel.informar("No se pudo guardar la foto. Intenta de nuevo.")
            }
        }
        cultivoEnCaptura = null
        uriTemporal = null
        comentarioEnCaptura = ""
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    val errorMotivo by viewModel.errorMotivo.collectAsStateWithLifecycle()
    cultivoARechazar?.let { id ->
        DialogoRechazo(
            error = errorMotivo,
            onMotivoCambiado = { viewModel.limpiarErrorMotivo() },
            onConfirmar = { motivo ->
                if (viewModel.rechazar(id, motivo)) cultivoARechazar = null
            },
            onCancelar = {
                viewModel.limpiarErrorMotivo()
                cultivoARechazar = null
            }
        )
    }

    val errorComentario by viewModel.errorComentario.collectAsStateWithLifecycle()
    cultivoAComentar?.let { id ->
        DialogoComentario(
            error = errorComentario,
            onComentarioCambiado = { viewModel.limpiarErrorComentario() },
            onConfirmar = { comentario ->
                if (viewModel.validarComentario(comentario)) {
                    // Comentario válido: ahora se toma la foto de respaldo
                    comentarioEnCaptura = comentario.trim()
                    val uri = crearUriFotografia(contexto)
                    uriTemporal = uri
                    cultivoEnCaptura = id
                    cultivoAComentar = null
                    lanzadorCamara.launch(uri)
                }
            },
            onCancelar = {
                viewModel.limpiarErrorComentario()
                cultivoAComentar = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Text(if (esEstudiante) "Mis cultivos" else "Cultivos registrados")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(onClick = onNuevoCultivo) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo cultivo")
                }
            }
        }
    ) { innerPadding ->
        if (cultivos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (esEstudiante) {
                        "Aún no tienes cultivos asignados"
                    } else {
                        "Aún no hay cultivos registrados"
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cultivos, key = { it.id }) { cultivo ->
                    TarjetaCultivo(
                        cultivo = cultivo,
                        esEstudiante = esEstudiante,
                        esAdministrador = esAdministrador,
                        onCambiarEstado = { viewModel.cambiarEstado(cultivo.id, it) },
                        onValidar = { viewModel.validar(cultivo.id) },
                        onRechazar = { cultivoARechazar = cultivo.id },
                        onMarcarRealizada = { cultivoAComentar = cultivo.id },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCultivo(
    cultivo: Cultivo,
    esEstudiante: Boolean,
    esAdministrador: Boolean,
    onCambiarEstado: (EstadoCultivo) -> Unit,
    onValidar: () -> Unit,
    onRechazar: () -> Unit,
    onMarcarRealizada: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoTarea = EstadoTarea.entries.firstOrNull { it.name == cultivo.estadoTarea }
        ?: EstadoTarea.PENDIENTE
    val fueRechazado = estadoTarea == EstadoTarea.PENDIENTE &&
            !cultivo.motivoRechazo.isNullOrBlank()
    val puedeEnviar = esEstudiante && estadoTarea == EstadoTarea.PENDIENTE
    val estadoCultivo = EstadoCultivo.entries.firstOrNull { it.name == cultivo.estado }
    // HU18: pendiente y con la fecha límite vencida
    val atrasada = cultivo.estaAtrasada()
    val estadoMostrado = if (atrasada) EstadoTarea.ATRASADA else estadoTarea

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (atrasada) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cultivo.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sector ID: ${cultivo.sectorId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha de siembra: ${cultivo.fechaSiembra}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Estimada de cosecha: ${cultivo.fechaCosecha}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Riego: cada ${cultivo.frecuenciaRiegoDias} días",
                style = MaterialTheme.typography.bodySmall
            )
            if (!cultivo.fechaLimite.isNullOrBlank()) {
                Text(
                    text = "Fecha límite de la tarea: ${cultivo.fechaLimite}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (esAdministrador) {
                // HU08: el administrador elige el nuevo estado desde un menú desplegable
                Spacer(Modifier.height(8.dp))
                CampoDesplegable(
                    valor = estadoCultivo?.etiqueta() ?: cultivo.estado,
                    etiqueta = "Estado del cultivo",
                    opciones = EstadoCultivo.entries.map { it.etiqueta() },
                    alSeleccionar = { onCambiarEstado(EstadoCultivo.entries[it]) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Estado: ${estadoCultivo?.etiqueta() ?: cultivo.estado}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            if (atrasada) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tarea: ${estadoMostrado.etiqueta()} (la fecha límite ya pasó)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Text(
                    text = "Tarea: ${estadoMostrado.etiqueta()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (fueRechazado) {
                Text(
                    text = "Rechazada por el administrador: ${cultivo.motivoRechazo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // HU12: el comentario enviado se ve mientras la tarea no esté pendiente de nuevo
            if (estadoTarea != EstadoTarea.PENDIENTE && !cultivo.comentarioEstudiante.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Comentario del estudiante: ${cultivo.comentarioEstudiante}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!cultivo.fotoUri.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = cultivo.fotoUri,
                    contentDescription = "Foto de la tarea",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            if (puedeEnviar) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onMarcarRealizada,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (fueRechazado) {
                            "Tomar nueva foto y reenviar"
                        } else {
                            "Marcar como realizada con foto"
                        }
                    )
                }
            }

            if (esAdministrador && estadoTarea == EstadoTarea.PENDIENTE_DE_VALIDACION) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rechazar")
                    }
                    Button(
                        onClick = onValidar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Validar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoRechazo(
    error: String?,
    onMotivoCambiado: () -> Unit,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Rechazar tarea") },
        text = {
            OutlinedTextField(
                value = motivo,
                onValueChange = {
                    motivo = it
                    onMotivoCambiado()
                },
                label = { Text("Motivo del rechazo") },
                placeholder = { Text("Ej: la foto no muestra el riego") },
                isError = error != null,
                trailingIcon = {
                    if (error != null) {
                        Icon(Icons.Default.Error, contentDescription = "Error")
                    }
                },
                supportingText = { error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(motivo) }) {
                Text("Rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DialogoComentario(
    error: String?,
    onComentarioCambiado: () -> Unit,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var comentario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Marcar tarea como realizada") },
        text = {
            OutlinedTextField(
                value = comentario,
                onValueChange = {
                    comentario = it
                    onComentarioCambiado()
                },
                label = { Text("Comentario") },
                placeholder = { Text("Ej: regué los surcos y retiré las malezas") },
                isError = error != null,
                trailingIcon = {
                    if (error != null) {
                        Icon(Icons.Default.Error, contentDescription = "Error")
                    }
                },
                supportingText = { error?.let { Text(it) } },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(comentario) }) {
                Text("Continuar con la foto")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
