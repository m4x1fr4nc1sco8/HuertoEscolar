package cl.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.Alerta
import cl.duoc.huertoescolar.domain.model.TipoAlerta
import cl.duoc.huertoescolar.ui.viewmodel.AlertasViewModel
import cl.duoc.huertoescolar.ui.viewmodel.Fabrica

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    onVolver: () -> Unit,
    viewModel: AlertasViewModel = viewModel(factory = Fabrica.alertas)
) {
    val alertas by viewModel.alertas.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Alertas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (alertas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alertas por ahora. No hay riegos ni cosechas que atender.",
                    textAlign = TextAlign.Center
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
                items(alertas, key = { "${it.cultivoId}-${it.tipo.name}" }) { alerta ->
                    TarjetaAlerta(alerta)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAlerta(alerta: Alerta) {
    val colores = MaterialTheme.colorScheme

    // Lo más urgente se destaca: vencida en rojo, "hoy" en el color principal, lo próximo en neutro
    val (fondo, contenido) = when (alerta.tipo) {
        TipoAlerta.COSECHA_VENCIDA -> colores.errorContainer to colores.onErrorContainer
        TipoAlerta.RIEGO_HOY, TipoAlerta.COSECHA_HOY ->
            colores.primaryContainer to colores.onPrimaryContainer
        TipoAlerta.RIEGO_PROXIMO, TipoAlerta.COSECHA_PROXIMA ->
            colores.surfaceVariant to colores.onSurfaceVariant
    }
    val icono = when (alerta.tipo) {
        TipoAlerta.RIEGO_HOY, TipoAlerta.RIEGO_PROXIMO -> Icons.Filled.WaterDrop
        TipoAlerta.COSECHA_HOY, TipoAlerta.COSECHA_PROXIMA -> Icons.Filled.Agriculture
        TipoAlerta.COSECHA_VENCIDA -> Icons.Filled.Warning
    }
    val (titulo, detalle) = textosDe(alerta)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = contenido)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(contenido.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = contenido)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = detalle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun textoDias(dias: Int): String = if (dias == 1) "1 día" else "$dias días"

// Título y detalle que se muestran para cada tipo de alerta
private fun textosDe(alerta: Alerta): Pair<String, String> = when (alerta.tipo) {
    TipoAlerta.RIEGO_HOY ->
        "Regar hoy: ${alerta.cultivoNombre}" to "Hoy toca el riego de este cultivo."
    TipoAlerta.RIEGO_PROXIMO ->
        "Riego próximo: ${alerta.cultivoNombre}" to
                "Toca regar en ${textoDias(alerta.dias)} (${alerta.fecha})."
    TipoAlerta.COSECHA_HOY ->
        "Cosecha hoy: ${alerta.cultivoNombre}" to "La cosecha estimada es hoy."
    TipoAlerta.COSECHA_PROXIMA ->
        "Cosecha próxima: ${alerta.cultivoNombre}" to
                "Faltan ${textoDias(alerta.dias)} para la cosecha estimada (${alerta.fecha})."
    TipoAlerta.COSECHA_VENCIDA ->
        "Cosecha atrasada: ${alerta.cultivoNombre}" to
                "La fecha estimada (${alerta.fecha}) pasó hace ${textoDias(alerta.dias)}. " +
                "Revisa el cultivo para no perder la cosecha."
}
