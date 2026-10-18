package cl.duoc.huertoescolar.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.ui.components.CampoTexto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import cl.duoc.huertoescolar.util.crearUriFotografia
import cl.duoc.huertoescolar.viewmodel.SectorUiState
import cl.duoc.huertoescolar.viewmodel.SectorViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorFormScreen(
    onVolver: () -> Unit,
    viewModel: SectorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Sectores") },
                navigationIcon = {
                    IconButton(onClick = { onVolver() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        SectorFormContenido(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SectorFormContenido(
    uiState: SectorUiState,
    viewModel: SectorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }

    val selectorFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.cambiarFoto(uri.toString())
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriPendiente?.let { viewModel.cambiarFoto(it.toString()) }
        }
        uriPendiente = null
    }

    val hayFoto = uiState.fotoUri != null

    val colorBoton by animateColorAsState(
        targetValue = if (hayFoto) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 600),
        label = "Color del botón"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Registrar sector",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Crossfade(targetState = hayFoto, label = "Estado de la foto") { tieneFoto ->
            Text(
                text = if (tieneFoto) {
                    "Foto referencial agregada"
                } else {
                    "Agrega una foto referencial (opcional)"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = hayFoto,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AsyncImage(
                model = uiState.fotoUri?.let { Uri.parse(it) },
                contentDescription = "Foto referencial del sector",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    selectorFotos.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                modifier = Modifier.weight(1f)
            ) {
                Text("Galería")
            }
            OutlinedButton(
                onClick = {
                    try {
                        val nuevaUri = crearUriFotografia(context)
                        uriPendiente = nuevaUri
                        camaraLauncher.launch(nuevaUri)
                    } catch (e: Exception) {
                        uriPendiente = null
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cámara")
            }
        }

        AnimatedVisibility(visible = hayFoto) {
            TextButton(onClick = { viewModel.quitarFoto() }) {
                Text("Quitar foto")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoTexto(
                valor = uiState.codigo,
                alCambiar = { viewModel.cambiarCodigo(it) },
                etiqueta = "Código",
                error = uiState.errorCodigo,
                placeholder = "Ej: S-01",
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = uiState.nombre,
                alCambiar = { viewModel.cambiarNombre(it) },
                etiqueta = "Nombre",
                error = uiState.errorNombre,
                placeholder = "Ej: Huerta de hortalizas",
                modifier = Modifier.weight(1f)
            )
        }

        CampoTexto(
            valor = uiState.ubicacion,
            alCambiar = { viewModel.cambiarUbicacion(it) },
            etiqueta = "Ubicación en el huerto",
            error = uiState.errorUbicacion,
            placeholder = "Ej: Patio norte",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sector en uso", modifier = Modifier.weight(1f))
            Switch(
                checked = uiState.enUso,
                onCheckedChange = { viewModel.cambiarEnUso(it) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.confirma,
                onCheckedChange = { viewModel.cambiarConfirma(it) }
            )
            Text("Confirmo que los datos son correctos")
        }
        uiState.errorConfirma?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.limpiar() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar")
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        }

        AnimatedVisibility(
            visible = uiState.mensaje.isNotEmpty(),
            enter = fadeIn() + expandVertically()
        ) {
            Text(
                text = uiState.mensaje,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectorFormScreenPreview() {
    HuertoEscolarTheme {
        SectorFormScreen(onVolver = {})
    }
}