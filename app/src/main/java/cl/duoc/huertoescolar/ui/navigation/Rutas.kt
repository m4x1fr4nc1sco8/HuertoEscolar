package cl.duoc.huertoescolar.ui.navigation

object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val SECTORES = "sectores"

    const val REGISTRO = "registro"

    const val USUARIOS = "usuarios"

    const val LISTA_SECTORES = "lista_sectores"

    const val CULTIVO_FORM = "cultivo_form"

    const val CULTIVOS = "cultivos"

    const val SECTORES_SERVIDOR = "sectores_servidor"

    const val SECTOR_EDITAR = "sector_editar/{sectorId}"

    fun sectorEditar(id: Int) = "sector_editar/$id"
}