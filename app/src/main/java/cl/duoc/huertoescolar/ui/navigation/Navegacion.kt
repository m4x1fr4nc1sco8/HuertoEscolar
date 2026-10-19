package cl.duoc.huertoescolar.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huertoescolar.data.SesionActual
import cl.duoc.huertoescolar.model.Rol
import cl.duoc.huertoescolar.ui.screens.AccesoDenegadoScreen
import cl.duoc.huertoescolar.ui.screens.CultivoFormScreen
import cl.duoc.huertoescolar.ui.screens.CultivosScreen // <-- Crearemos este screen a continuación
import cl.duoc.huertoescolar.ui.screens.InicioScreen
import cl.duoc.huertoescolar.ui.screens.LoginScreen
import cl.duoc.huertoescolar.ui.screens.RegistroScreen
import cl.duoc.huertoescolar.ui.screens.SectorFormScreen
import cl.duoc.huertoescolar.ui.screens.SectoresScreen
import cl.duoc.huertoescolar.ui.screens.UsuariosScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    val usuarioActual by SesionActual.usuario.collectAsStateWithLifecycle()
    val esAdministrador = usuarioActual?.tipoUsuario == Rol.ADMINISTRADOR.name

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
                onVolver = { navController.popBackStack() },
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
                onIrACultivos = { navController.navigate(Rutas.CULTIVOS) }, // <-- NUEVO CALLBACK
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrAUsuarios = { navController.navigate(Rutas.USUARIOS) },
                onCerrarSesion = {
                    SesionActual.cerrar()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.SECTORES) {
            if (esAdministrador) {
                SectorFormScreen(onVolver = { navController.popBackStack() })
            } else {
                AccesoDenegadoScreen(onVolver = { navController.popBackStack() })
            }
        }
        composable(Rutas.LISTA_SECTORES) {
            SectoresScreen(
                onVolver = { navController.popBackStack() },
                onNuevoSector = { navController.navigate(Rutas.SECTORES) },
                puedeCrear = esAdministrador
            )
        }
        composable(Rutas.USUARIOS) {
            if (esAdministrador) {
                UsuariosScreen(onVolver = { navController.popBackStack() })
            } else {
                AccesoDenegadoScreen(onVolver = { navController.popBackStack() })
            }
        }
        composable(Rutas.CULTIVO_FORM) {
            if (esAdministrador) {
                CultivoFormScreen(onVolver = { navController.popBackStack() })
            } else {
                AccesoDenegadoScreen(onVolver = { navController.popBackStack() })
            }
        }
        // NUEVA RUTA PARA MOSTRAR LA LISTA DE CULTIVOS
        composable(Rutas.CULTIVOS) {
            CultivosScreen(
                onVolver = { navController.popBackStack() },
                onNuevoCultivo = { navController.navigate(Rutas.CULTIVO_FORM) },
                puedeCrear = esAdministrador
            )
        }
    }
}