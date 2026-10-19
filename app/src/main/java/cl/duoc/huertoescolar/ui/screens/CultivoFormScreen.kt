package cl.duoc.huertoescolar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.components.CampoDesplegable
import cl.duoc.huertoescolar.ui.components.CampoTexto
import cl.duoc.huertoescolar.util.etiqueta
import cl.duoc.huertoescolar.viewmodel.CultivoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    onVolver: () -> Unit,
    viewModel: CultivoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sectores by viewModel.sectores.collectAsStateWithLifecycle()
    val estudiantes by viewModel.estudiantes.collectAsStateWithLifecycle()
    val estados = EstadoCultivo.entries

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Cultivos") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Registrar cultivo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            CampoTexto(
                valor = uiState.nombre,
                alCambiar = { viewModel.cambiarNombre(it) },
                etiqueta = "Nombre del cultivo",
                error = uiState.errorNombre,
                placeholder = "Ej: Lechuga",
                modifier = Modifier.fillMaxWidth()
            )

            if (sectores.isEmpty()) {
                Text(
                    "No hay sectores registrados. Registra un sector antes de crear un cultivo.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            CampoDesplegable(
                valor = uiState.sectorTexto,
                etiqueta = "Sector",
                opciones = sectores.map { "${it.codigo} · ${it.nombre}" },
                alSeleccionar = { indice -> viewModel.seleccionarSector(sectores[indice]) },
                error = uiState.errorSector,
                modifier = Modifier.fillMaxWidth()
            )

            if (estudiantes.isEmpty()) {
                Text(
                    "No hay estudiantes registrados. Registra un usuario con perfil estudiante para asignarle el cultivo.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            CampoDesplegable(
                valor = uiState.estudianteTexto,
                etiqueta = "Estudiante asignado",
                opciones = estudiantes.map { "${it.nombre} ${it.apellido}" },
                alSeleccionar = { indice -> viewModel.seleccionarEstudiante(estudiantes[indice]) },
                error = uiState.errorEstudiante,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = uiState.fechaSiembra,
                alCambiar = { viewModel.cambiarSiembra(it) },
                etiqueta = "Fecha de siembra",
                error = uiState.errorSiembra,
                placeholder = "dd/mm/aaaa",
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = uiState.fechaCosecha,
                alCambiar = { viewModel.cambiarCosecha(it) },
                etiqueta = "Fecha estimada de cosecha",
                error = uiState.errorCosecha,
                placeholder = "dd/mm/aaaa",
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = uiState.frecuenciaRiego,
                alCambiar = { viewModel.cambiarRiego(it) },
                etiqueta = "Riego cada (días)",
                error = uiState.errorRiego,
                placeholder = "Ej: 2",
                modifier = Modifier.fillMaxWidth()
            )

            CampoDesplegable(
                valor = uiState.estado.etiqueta(),
                etiqueta = "Estado del cultivo",
                opciones = estados.map { it.etiqueta() },
                alSeleccionar = { indice -> viewModel.cambiarEstado(estados[indice]) },
                modifier = Modifier.fillMaxWidth()
            )

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
}
