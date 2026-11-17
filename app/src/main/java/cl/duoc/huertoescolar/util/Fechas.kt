package cl.duoc.huertoescolar.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Fecha de hoy con el formato que usa la app (dd/MM/yyyy)
fun fechaActual(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
