package cl.duoc.huertoescolar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = SobreVerdeContenedor,
    secondary = HojaSecundario,
    onSecondary = Color.White,
    secondaryContainer = HojaContenedor,
    onSecondaryContainer = SobreHojaContenedor,
    tertiary = TierraTerciario,
    onTertiary = Color.White,
    tertiaryContainer = TierraContenedor,
    onTertiaryContainer = SobreTierraContenedor,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    surfaceVariant = VariantClaro,
    onSurfaceVariant = TextoVariantClaro,
    outline = BordeClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdePrincipalOscuro,
    onPrimary = SobreVerdePrincipalOscuro,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = SobreVerdeContenedorOscuro,
    secondary = HojaSecundarioOscuro,
    onSecondary = SobreHojaSecundarioOscuro,
    secondaryContainer = HojaContenedorOscuro,
    onSecondaryContainer = SobreHojaContenedorOscuro,
    tertiary = TierraTerciarioOscuro,
    onTertiary = SobreTierraTerciarioOscuro,
    tertiaryContainer = TierraContenedorOscuro,
    onTertiaryContainer = SobreTierraContenedorOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = VariantOscuro,
    onSurfaceVariant = TextoVariantOscuro,
    outline = BordeOscuro
)

@Composable
fun HuertoEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}