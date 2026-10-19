package cl.duoc.huertoescolar.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.model.EstadoSector
import cl.duoc.huertoescolar.viewmodel.SectoresViewModel
import coil3.compose.AsyncImage

private fun textoEstado(estado: String): String = when (estado) {
    EstadoSector.DISPONIBLE.name -> "Disponible"
    EstadoSector.EN_USO.name -> "En uso"
    EstadoSector.EN_MANTENIMIENTO.name -> "En mantenimiento"
    EstadoSector.FUERA_DE_USO.name -> "Fuera de uso"
    else -> estado
}

private fun colorEstado(estado: String): Color = when (estado) {
    EstadoSector.EN_USO.name -> Color(0xFFC8E6C9)
    EstadoSector.EN_MANTENIMIENTO.name -> Color(0xFFFFF9C4)
    EstadoSector.FUERA_DE_USO.name -> Color(0xFFFFCDD2)
    else -> Color(0xFFEEEEEE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectoresScreen(
    onVolver: () -> Unit,
    onNuevoSector: () -> Unit,
    puedeCrear: Boolean,
    viewModel: SectoresViewModel = viewModel()
) {
    val sectores by viewModel.sectores.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Sectores registrados") },
                navigationIcon = {
                    IconButton(onClick = { onVolver() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(onClick = onNuevoSector) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo sector")
                }
            }
        }
    ) { innerPadding ->
        if (sectores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay sectores registrados")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sectores, key = { it.id }) { sector ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorEstado(sector.estado),
                            contentColor = Color.Black
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            sector.fotoUri?.let { foto ->
                                AsyncImage(
                                    model = Uri.parse(foto),
                                    contentDescription = "Foto del sector",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            Text(
                                text = "${sector.codigo} · ${sector.nombre}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = "Ubicación: ${sector.ubicacion}")
                            Text(
                                text = "Estado: ${textoEstado(sector.estado)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Creado el ${sector.fechaCreacion}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}