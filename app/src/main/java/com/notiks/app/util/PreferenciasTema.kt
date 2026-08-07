package com.notiks.app.util

import android.content.Context

/** Una opción de color de fondo: su nombre, su valor hexadecimal, y si es un tono oscuro. */
data class OpcionColorFondo(val nombre: String, val hex: String, val esOscuro: Boolean)

/**
 * Guarda y recupera el color de fondo elegido por el usuario para toda la
 * app, usando SharedPreferences (persiste entre aperturas de la app).
 */
object PreferenciasTema {
    private const val PREFS = "notiks_prefs"
    private const val KEY_COLOR_FONDO = "color_fondo"

    val coloresDisponibles = listOf(
        // Modo claro
        OpcionColorFondo("Violeta (original)", "#F7F5FF", esOscuro = false),
        OpcionColorFondo("Celeste", "#EAF4FF", esOscuro = false),
        OpcionColorFondo("Menta", "#E9FBF3", esOscuro = false),
        OpcionColorFondo("Durazno", "#FFF1E8", esOscuro = false),
        OpcionColorFondo("Rosa suave", "#FDEFFA", esOscuro = false),
        OpcionColorFondo("Gris neutro", "#F2F2F4", esOscuro = false),
        // Modo oscuro
        OpcionColorFondo("Violeta oscuro (original)", "#2C1B7A", esOscuro = true),
        OpcionColorFondo("Negro azulado", "#14121F", esOscuro = true),
        OpcionColorFondo("Gris carbón", "#1E1E22", esOscuro = true),
        OpcionColorFondo("Verde bosque oscuro", "#0F211B", esOscuro = true)
    )

    fun obtenerColorGuardado(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_COLOR_FONDO, coloresDisponibles.first().hex)
            ?: coloresDisponibles.first().hex
    }

    fun guardarColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COLOR_FONDO, colorHex)
            .apply()
    }

    /** Indica si el color guardado corresponde a una opción de modo oscuro. */
    fun esColorOscuro(hex: String): Boolean =
        coloresDisponibles.find { it.hex.equals(hex, ignoreCase = true) }?.esOscuro ?: false
}
