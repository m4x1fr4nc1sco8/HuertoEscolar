package cl.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.R
import cl.duoc.huertoescolar.ui.components.CampoTexto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import cl.duoc.huertoescolar.viewmodel.SectorUiState
import cl.duoc.huertoescolar.viewmodel.SectorViewModel

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
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Imagen referencial del sector"
            )
        }

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

        if (uiState.mensaje.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(uiState.mensaje, color = MaterialTheme.colorScheme.primary)
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