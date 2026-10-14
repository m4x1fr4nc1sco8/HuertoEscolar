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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.R
import cl.duoc.huertoescolar.model.EstadoSector
import cl.duoc.huertoescolar.ui.components.CampoTexto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import cl.duoc.huertoescolar.util.Validaciones

@Composable
fun SectorFormScreen(modifier: Modifier = Modifier) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var enUso by remember { mutableStateOf(false) }
    var confirma by remember { mutableStateOf(false) }

    var errorCodigo by remember { mutableStateOf<String?>(null) }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }
    var errorConfirma by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Registrar sector", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
                valor = codigo,
                alCambiar = { codigo = it; errorCodigo = null },
                etiqueta = "Código",
                error = errorCodigo,
                placeholder = "Ej: S-01",
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = nombre,
                alCambiar = { nombre = it; errorNombre = null },
                etiqueta = "Nombre",
                error = errorNombre,
                placeholder = "Ej: Huerta de hortalizas",
                modifier = Modifier.weight(1f)
            )
        }

        CampoTexto(
            valor = ubicacion,
            alCambiar = { ubicacion = it; errorUbicacion = null },
            etiqueta = "Ubicación en el huerto",
            error = errorUbicacion,
            placeholder = "Ej: Patio norte",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sector en uso", modifier = Modifier.weight(1f))
            Switch(checked = enUso, onCheckedChange = { enUso = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = confirma,
                onCheckedChange = { confirma = it; errorConfirma = null }
            )
            Text("Confirmo que los datos son correctos")
        }
        errorConfirma?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    codigo = ""; nombre = ""; ubicacion = ""
                    enUso = false; confirma = false
                    errorCodigo = null; errorNombre = null
                    errorUbicacion = null; errorConfirma = null
                    mensaje = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }

            Button(
                onClick = {
                    val rCodigo = Validaciones.validarObligatorio(codigo, "El código")
                    val rNombre = Validaciones.validarNombre(nombre, "El nombre del sector")
                    val rUbicacion = Validaciones.validarObligatorio(ubicacion, "La ubicación")
                    errorCodigo = rCodigo.mensaje
                    errorNombre = rNombre.mensaje
                    errorUbicacion = rUbicacion.mensaje
                    errorConfirma = if (confirma) null else "Debes confirmar los datos"

                    if (rCodigo.esValido && rNombre.esValido && rUbicacion.esValido && confirma) {
                        val estado = if (enUso) EstadoSector.EN_USO else EstadoSector.DISPONIBLE
                        println("Sector: $codigo | $nombre | $ubicacion | $estado")
                        mensaje = "Sector guardado (prueba)"
                    } else {
                        mensaje = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar")
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectorFormScreenPreview() {
    HuertoEscolarTheme {
        SectorFormScreen()
    }
}