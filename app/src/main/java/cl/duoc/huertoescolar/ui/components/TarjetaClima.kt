package cl.duoc.huertoescolar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.domain.model.Clima
import cl.duoc.huertoescolar.ui.viewmodel.ClimaUiState
import kotlin.math.roundToInt

// Clima de San Bernardo (API externa Open-Meteo) para saber si conviene regar
@Composable
fun TarjetaClima(
    estado: ClimaUiState,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Clima en San Bernardo",
                style = MaterialTheme.typography.titleSmall
            )

            when (estado) {
                ClimaUiState.Cargando -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Cargando clima...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is ClimaUiState.Error -> {
                    Text(
                        text = estado.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = onReintentar) { Text("Reintentar") }
                }

                is ClimaUiState.Exito -> ContenidoClima(estado.clima)
            }
        }
    }
}

@Composable
private fun ContenidoClima(clima: Clima) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = clima.icono, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "${clima.temperatura.roundToInt()}°C · ${clima.descripcion}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Humedad ${clima.humedad}% · Viento ${clima.vientoKmH.roundToInt()} km/h",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        clima.pronostico.forEach { dia ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dia.fecha, style = MaterialTheme.typography.labelSmall)
                Text(text = dia.icono, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${dia.minima.roundToInt()}° / ${dia.maxima.roundToInt()}°",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Lluvia ${dia.probabilidadLluvia}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
