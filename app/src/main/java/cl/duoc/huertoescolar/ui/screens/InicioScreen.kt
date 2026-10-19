package cl.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    nombreUsuario: String,
    esAdministrador: Boolean,
    onIrASectores: () -> Unit,
    onIrAListaSectores: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrAUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Huerto Escolar") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hola, $nombreUsuario", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = if (esAdministrador) "Perfil administrador" else "Perfil estudiante",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))

            if (esAdministrador) {
                Button(onClick = onIrASectores, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar sector")
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = onIrAListaSectores, modifier = Modifier.fillMaxWidth()) {
                Text("Sectores registrados")
            }
            Spacer(Modifier.height(8.dp))

            if (esAdministrador) {
                Button(onClick = onIrARegistro, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar usuario")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onIrAUsuarios, modifier = Modifier.fillMaxWidth()) {
                    Text("Usuarios registrados")
                }
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InicioScreenPreview() {
    HuertoEscolarTheme {
        InicioScreen(
            nombreUsuario = "Camila",
            esAdministrador = true,
            onIrASectores = {},
            onIrAListaSectores = {},
            onIrARegistro = {},
            onIrAUsuarios = {},
            onCerrarSesion = {}
        )
    }
}