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
fun NotiksTheme(colorFondo: Color? = null, content: @Composable () -> Unit) {
    // Si el usuario eligió un color de fondo, se respeta siempre (con la
    // paleta clara, que tiene texto oscuro legible sobre tonos pastel),
    // sin importar si el sistema está en modo oscuro o claro. Solo se usa
    // el tema oscuro automático cuando no hay una elección explícita.
    val colors = when {
        colorFondo != null -> LightColors.copy(background = colorFondo)
        isSystemInDarkTheme() -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
