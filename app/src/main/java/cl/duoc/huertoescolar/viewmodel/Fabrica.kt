package cl.duoc.huertoescolar.viewmodel
import cl.duoc.huertoescolar.data.CultivoRepository
import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.huertoescolar.data.AppDatabase
import cl.duoc.huertoescolar.data.SectorRepository
import cl.duoc.huertoescolar.data.UsuarioRepository
import cl.duoc.huertoescolar.util.guardarFotoPermanente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object Fabrica {

    private fun Application.cultivoRepository() =
        CultivoRepository(AppDatabase.getInstance(this).cultivoDao())

    private fun CreationExtras.aplicacion(): Application =
        this[APPLICATION_KEY] as Application

    private fun Application.sectorRepository() =
        SectorRepository(AppDatabase.getInstance(this).sectorDao())

    private fun Application.usuarioRepository() =
        UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())

    val cultivo = viewModelFactory {
        initializer {
            val app = aplicacion()
            CultivoViewModel(
                repository = app.cultivoRepository(),
                sectorRepository = app.sectorRepository(),
                usuarioRepository = app.usuarioRepository()
            )
        }
    }

    val cultivos = viewModelFactory {
        initializer { CultivosViewModel(aplicacion().cultivoRepository()) }
    }

    val sector = viewModelFactory {
        initializer {
            val app = aplicacion()
            SectorViewModel(
                repository = app.sectorRepository(),
                guardarFoto = { origen ->
                    withContext(Dispatchers.IO) {
                        guardarFotoPermanente(app, Uri.parse(origen))
                    }
                }
            )
        }
    }

    val sectores = viewModelFactory {
        initializer { SectoresViewModel(aplicacion().sectorRepository()) }
    }

    val login = viewModelFactory {
        initializer { LoginViewModel(aplicacion().usuarioRepository()) }
    }

    val registro = viewModelFactory {
        initializer { RegistroViewModel(aplicacion().usuarioRepository()) }
    }

    val usuarios = viewModelFactory {
        initializer { UsuariosViewModel(aplicacion().usuarioRepository()) }
    }
}
