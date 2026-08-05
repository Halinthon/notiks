package com.notiks.app.util

import com.notiks.app.data.Origen

object OrigenDetector {
    fun detectar(textoCompartido: String): Origen {
        val texto = textoCompartido.lowercase()
        return when {
            texto.contains("youtube.com") || texto.contains("youtu.be") -> Origen.YOUTUBE
            texto.contains("x.com") || texto.contains("twitter.com") -> Origen.X
            texto.contains("instagram.com") -> Origen.INSTAGRAM
            // Google Discover no tiene dominio propio: los enlaces llegan reescritos
            // vía google.com/url o share.google, así que se detectan por ese patrón.
            texto.contains("google.com/url") || texto.contains("share.google") -> Origen.DISCOVER
            texto.contains("http://") || texto.contains("https://") -> Origen.WEB
            else -> Origen.TEXTO
        }
    }

    /** Extrae la primera URL encontrada en el texto compartido, si existe. */
    fun extraerUrl(texto: String): String? {
        val regex = Regex("https?://\\S+")
        return regex.find(texto)?.value
    }

    fun nombreLegible(origen: Origen): String = when (origen) {
        Origen.YOUTUBE -> "YouTube"
        Origen.X -> "X"
        Origen.INSTAGRAM -> "Instagram"
        Origen.DISCOVER -> "Google Discover"
        Origen.WEB -> "Web"
        Origen.TEXTO -> "Nota"
    }
}
