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
    val base = if (isSystemInDarkTheme()) DarkColors else LightColors
    // El selector de color de fondo solo aplica en modo claro; en modo oscuro
    // se respeta la paleta oscura original para mantener buen contraste.
    val colors = if (colorFondo != null && !isSystemInDarkTheme()) {
        base.copy(background = colorFondo)
    } else {
        base
    }
    MaterialTheme(colorScheme = colors, content = content)
}
