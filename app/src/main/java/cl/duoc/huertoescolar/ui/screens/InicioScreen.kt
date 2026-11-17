package cl.duoc.huertoescolar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme

// Datos de cada tarjeta del menú
private data class OpcionMenu(
    val titulo: String,
    val textoBoton: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorContenido: Color,
    val onClick: () -> Unit
)

@Composable
fun InicioScreen(
    nombreUsuario: String,
    esAdministrador: Boolean,
    onIrASectores: () -> Unit,
    onIrAListaSectores: () -> Unit,
    onIrACultivoForm: () -> Unit,
    onIrACultivos: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrAUsuarios: () -> Unit,
    onIrAServidor: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    // Las opciones se arman según el rol (mismas reglas que antes)
    val opciones = buildList {
        if (esAdministrador) {
            add(
                OpcionMenu(
                    "Registrar sector", "Crear", Icons.Filled.AddLocationAlt,
                    colores.primaryContainer, colores.onPrimaryContainer, onIrASectores
                )
            )
        }
        add(
            OpcionMenu(
                "Sectores registrados", "Ver", Icons.Filled.Map,
                colores.secondaryContainer, colores.onSecondaryContainer, onIrAListaSectores
            )
        )
        if (esAdministrador) {
            add(
                OpcionMenu(
                    "Registrar cultivo", "Crear", Icons.Filled.Eco,
                    colores.tertiaryContainer, colores.onTertiaryContainer, onIrACultivoForm
                )
            )
        }
        add(
            OpcionMenu(
                "Cultivos registrados", "Ver", Icons.Filled.Grass,
                colores.primaryContainer, colores.onPrimaryContainer, onIrACultivos
            )
        )
        if (esAdministrador) {
            add(
                OpcionMenu(
                    "Registrar usuario", "Crear", Icons.Filled.PersonAdd,
                    colores.secondaryContainer, colores.onSecondaryContainer, onIrARegistro
                )
            )
            add(
                OpcionMenu(
                    "Usuarios registrados", "Ver", Icons.Filled.Groups,
                    colores.tertiaryContainer, colores.onTertiaryContainer, onIrAUsuarios
                )
            )
            add(
                OpcionMenu(
                    "Sectores en el servidor", "Ver", Icons.Filled.Cloud,
                    colores.primaryContainer, colores.onPrimaryContainer, onIrAServidor
                )
            )
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CabeceraInicio(
                nombreUsuario = nombreUsuario,
                esAdministrador = esAdministrador
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(opciones) { opcion ->
                    TarjetaMenu(opcion)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraInicio(
    nombreUsuario: String,
    esAdministrador: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Hola, $nombreUsuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (esAdministrador) "Perfil administrador" else "Perfil estudiante",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TarjetaMenu(opcion: OpcionMenu) {
    Card(
        onClick = opcion.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = opcion.colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(opcion.colorContenido.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = opcion.icono,
                    contentDescription = null,
                    tint = opcion.colorContenido,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = opcion.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = opcion.colorContenido,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = opcion.onClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                Text(opcion.textoBoton, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview(showBackground = true, name = "Administrador")
@Composable
fun InicioScreenAdminPreview() {
    HuertoEscolarTheme {
        InicioScreen(
            nombreUsuario = "Camila",
            esAdministrador = true,
            onIrASectores = {},
            onIrAListaSectores = {},
            onIrACultivoForm = {},
            onIrACultivos = {},
            onIrARegistro = {},
            onIrAUsuarios = {},
            onIrAServidor = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true, name = "Estudiante")
@Composable
fun InicioScreenEstudiantePreview() {
    HuertoEscolarTheme {
        InicioScreen(
            nombreUsuario = "Pedro",
            esAdministrador = false,
            onIrASectores = {},
            onIrAListaSectores = {},
            onIrACultivoForm = {},
            onIrACultivos = {},
            onIrARegistro = {},
            onIrAUsuarios = {},
            onIrAServidor = {},
            onCerrarSesion = {}
        )
    }
}