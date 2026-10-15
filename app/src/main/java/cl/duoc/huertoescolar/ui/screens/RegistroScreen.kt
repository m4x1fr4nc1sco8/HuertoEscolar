package cl.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.ui.components.CampoTexto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import cl.duoc.huertoescolar.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Registro de usuario") },
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(
                    valor = viewModel.nombre,
                    alCambiar = { viewModel.cambiarNombre(it) },
                    etiqueta = "Nombre",
                    error = viewModel.errorNombre,
                    icono = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    valor = viewModel.apellido,
                    alCambiar = { viewModel.cambiarApellido(it) },
                    etiqueta = "Apellido",
                    error = viewModel.errorApellido,
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                valor = viewModel.correo,
                alCambiar = { viewModel.cambiarCorreo(it) },
                etiqueta = "Correo",
                error = viewModel.errorCorreo,
                placeholder = "Ej: nombre@huerto.cl",
                icono = Icons.Default.Email,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = viewModel.fechaNacimiento,
                alCambiar = { viewModel.cambiarFecha(it) },
                etiqueta = "Fecha de nacimiento",
                error = viewModel.errorFecha,
                placeholder = "dd/mm/aaaa",
                icono = Icons.Default.DateRange,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = viewModel.clave,
                alCambiar = { viewModel.cambiarClave(it) },
                etiqueta = "Contraseña",
                error = viewModel.errorClave,
                icono = Icons.Default.Lock,
                esClave = true,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = viewModel.repetirClave,
                alCambiar = { viewModel.cambiarRepetir(it) },
                etiqueta = "Repetir contraseña",
                error = viewModel.errorRepetir,
                icono = Icons.Default.Lock,
                esClave = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Perfil administrador (docente o tallerista)",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = viewModel.esAdministrador,
                    onCheckedChange = { viewModel.cambiarEsAdministrador(it) }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.aceptaTerminos,
                    onCheckedChange = { viewModel.cambiarTerminos(it) }
                )
                Text("Acepto los términos y condiciones")
            }
            viewModel.errorTerminos?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.registrar() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }

            if (viewModel.mensaje.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(viewModel.mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    HuertoEscolarTheme {
        RegistroScreen(onVolver = {})
    }
}