package cl.duoc.huertoescolar.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.huertoescolar.data.session.SesionActual
import cl.duoc.huertoescolar.domain.model.Rol
import cl.duoc.huertoescolar.ui.screens.AccesoDenegadoScreen
import cl.duoc.huertoescolar.ui.screens.CultivoFormScreen
import cl.duoc.huertoescolar.ui.screens.CultivosScreen
import cl.duoc.huertoescolar.ui.screens.InicioScreen
import cl.duoc.huertoescolar.ui.screens.LoginScreen
import cl.duoc.huertoescolar.ui.screens.RegistroScreen
import cl.duoc.huertoescolar.ui.screens.SectorFormScreen
import cl.duoc.huertoescolar.ui.screens.SectoresScreen
import cl.duoc.huertoescolar.ui.screens.SectoresServidorScreen
import cl.duoc.huertoescolar.ui.screens.UsuariosScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    val usuarioActual by SesionActual.usuario.collectAsStateWithLifecycle()
    val esAdministrador = usuarioActual?.tipoUsuario == Rol.ADMINISTRADOR.name
    val volver: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onVolver = volver,
                puedeElegirPerfil = esAdministrador
            )
        }
        composable(Rutas.INICIO) {
            InicioScreen(
                nombreUsuario = usuarioActual?.nombre ?: "",
                esAdministrador = esAdministrador,
                onIrASectores = { navController.navigate(Rutas.SECTORES) },
                onIrAListaSectores = { navController.navigate(Rutas.LISTA_SECTORES) },
                onIrACultivoForm = { navController.navigate(Rutas.CULTIVO_FORM) },
                onIrACultivos = { navController.navigate(Rutas.CULTIVOS) },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrAUsuarios = { navController.navigate(Rutas.USUARIOS) },
                onIrAServidor = { navController.navigate(Rutas.SECTORES_SERVIDOR) },
                onCerrarSesion = {
                    SesionActual.cerrar()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.SECTORES) {
            SoloAdministrador(esAdministrador, volver) {
                SectorFormScreen(onVolver = volver)
            }
        }
        composable(Rutas.LISTA_SECTORES) {
            SectoresScreen(
                onVolver = volver,
                onNuevoSector = { navController.navigate(Rutas.SECTORES) },
                onEditarSector = { id -> navController.navigate(Rutas.sectorEditar(id)) },
                puedeCrear = esAdministrador
            )
        }
        composable(
            route = Rutas.SECTOR_EDITAR,
            arguments = listOf(navArgument("sectorId") { type = NavType.IntType })
        ) { entrada ->
            SoloAdministrador(esAdministrador, volver) {
                SectorFormScreen(
                    onVolver = volver,
                    sectorId = entrada.arguments?.getInt("sectorId")
                )
            }
        }
        composable(Rutas.USUARIOS) {
            SoloAdministrador(esAdministrador, volver) {
                UsuariosScreen(onVolver = volver)
            }
        }
        composable(Rutas.CULTIVO_FORM) {
            SoloAdministrador(esAdministrador, volver) {
                CultivoFormScreen(onVolver = volver)
            }
        }
        composable(Rutas.CULTIVOS) {
            CultivosScreen(
                onVolver = volver,
                onNuevoCultivo = { navController.navigate(Rutas.CULTIVO_FORM) },
                puedeCrear = esAdministrador
            )
        }
        composable(Rutas.SECTORES_SERVIDOR) {
            SoloAdministrador(esAdministrador, volver) {
                SectoresServidorScreen(onVolver = volver)
            }
        }
    }
}

// Muestra la pantalla solo si el usuario es administrador; si no, "acceso denegado"
@Composable
private fun SoloAdministrador(
    esAdministrador: Boolean,
    onVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    if (esAdministrador) {
        contenido()
    } else {
        AccesoDenegadoScreen(onVolver = onVolver)
    }
}
