package cl.duoc.huertoescolar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huertoescolar.ui.screens.InicioScreen
import cl.duoc.huertoescolar.ui.screens.LoginScreen
import cl.duoc.huertoescolar.ui.screens.RegistroScreen
import cl.duoc.huertoescolar.ui.screens.SectorFormScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
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
    }
}