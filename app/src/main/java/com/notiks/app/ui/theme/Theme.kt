package com.notiks.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = NotiksVioleta,
    onPrimary = Color.White,
    secondary = NotiksCoral,
    tertiary = NotiksAmarillo,
    background = NotiksFondo,
    surface = NotiksSuperficie,
    onBackground = NotiksTexto,
    onSurface = NotiksTexto
)

private val DarkColors = darkColorScheme(
    primary = NotiksVioleta,
    secondary = NotiksCoral,
    tertiary = NotiksAmarillo,
    background = NotiksVioletaOscuro,
    surface = Color(0xFF211648)
)

@Composable
fun NotiksTheme(colorFondo: Color? = null, esOscuro: Boolean = false, content: @Composable () -> Unit) {
    // Si el usuario eligió un color de fondo, se respeta siempre, usando la
    // paleta clara (texto oscuro) o la oscura (texto claro) según el color
    // elegido sea claro u oscuro — sin importar el modo del sistema.
    // Solo se usa el tema oscuro automático cuando no hay elección explícita.
    val colors = when {
        colorFondo != null && esOscuro -> DarkColors.copy(background = colorFondo, surface = colorFondo)
        colorFondo != null -> LightColors.copy(background = colorFondo)
        isSystemInDarkTheme() -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
