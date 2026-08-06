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
fun NotiksTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
