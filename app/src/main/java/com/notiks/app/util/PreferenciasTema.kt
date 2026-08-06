package com.notiks.app.util

import android.content.Context

/**
 * Guarda y recupera el color de fondo elegido por el usuario para toda la
 * app, usando SharedPreferences (persiste entre aperturas de la app).
 */
object PreferenciasTema {
    private const val PREFS = "notiks_prefs"
    private const val KEY_COLOR_FONDO = "color_fondo"

    /** Nombre visible junto a su color hexadecimal. */
    val coloresDisponibles = listOf(
        "Violeta (original)" to "#F7F5FF",
        "Celeste" to "#EAF4FF",
        "Menta" to "#E9FBF3",
        "Durazno" to "#FFF1E8",
        "Rosa suave" to "#FDEFFA",
        "Gris neutro" to "#F2F2F4"
    )

    fun obtenerColorGuardado(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_COLOR_FONDO, coloresDisponibles.first().second)
            ?: coloresDisponibles.first().second
    }

    fun guardarColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COLOR_FONDO, colorHex)
            .apply()
    }
}
