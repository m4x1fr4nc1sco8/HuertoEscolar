package cl.duoc.huertoescolar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huertoescolar.ui.screens.InicioScreen
import cl.duoc.huertoescolar.ui.screens.LoginScreen
import cl.duoc.huertoescolar.ui.screens.RegistroScreen
import cl.duoc.huertoescolar.ui.screens.SectorFormScreen
import cl.duoc.huertoescolar.ui.screens.SectoresScreen
import cl.duoc.huertoescolar.ui.screens.UsuariosScreen
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun Navegacion() {
    val navController = rememberNavController()

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
            RegistroScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.INICIO) {
            InicioScreen(
                onIrASectores = { navController.navigate(Rutas.SECTORES) },
                onIrAListaSectores = { navController.navigate(Rutas.LISTA_SECTORES) },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrAUsuarios = { navController.navigate(Rutas.USUARIOS) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.SECTORES) {
            SectorFormScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.LISTA_SECTORES) {
            SectoresScreen(
                onVolver = { navController.popBackStack() },
                onNuevoSector = { navController.navigate(Rutas.SECTORES) }
            )
        }
        composable(Rutas.USUARIOS) {
            UsuariosScreen(onVolver = { navController.popBackStack() })
        }
    }
}