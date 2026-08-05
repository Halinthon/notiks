package com.notiks.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object ResumenFetcher {

    private const val MAX_CARACTERES_LEIDOS = 200_000

    /**
     * Recorta un texto a un máximo de [max] palabras, sin cortar palabras a la mitad.
     */
    fun limitarPalabras(texto: String, max: Int = 20): String {
        val palabras = texto.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (palabras.size <= max) {
            palabras.joinToString(" ")
        } else {
            palabras.take(max).joinToString(" ") + "…"
        }
    }

    /**
     * Cuando la app de origen solo comparte el enlace (sin título ni descripción),
     * este método entra a la página y toma el título / descripción reales del
     * contenido (og:description, og:title o <title>) para armar un resumen legible,
     * en vez de dejar guardado solo el link.
     */
    suspend fun obtenerResumenDesdeUrl(urlTexto: String): String? = withContext(Dispatchers.IO) {
        try {
            val conexion = (URL(urlTexto).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) NotiksApp/1.0"
                )
            }
            conexion.connect()

            if (conexion.responseCode !in 200..299) {
                conexion.disconnect()
                return@withContext null
            }

            val html = conexion.inputStream.bufferedReader().use { lector ->
                val buffer = CharArray(MAX_CARACTERES_LEIDOS)
                val leidos = lector.read(buffer)
                if (leidos > 0) String(buffer, 0, leidos) else ""
            }
            conexion.disconnect()

            val ogDescription = extraerMeta(html, "og:description")
            val ogTitle = extraerMeta(html, "og:title")
            val tituloPlano = Regex(
                "<title[^>]*>(.*?)</title>",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ).find(html)?.groupValues?.get(1)

            val candidato = ogDescription ?: ogTitle ?: tituloPlano
            candidato
                ?.let { decodificarHtml(it) }
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun extraerMeta(html: String, propiedad: String): String? {
        // Cubre tanto property="og:x" content="..." como content="..." property="og:x"
        val patrones = listOf(
            Regex(
                "<meta[^>]+property=[\"']$propiedad[\"'][^>]+content=[\"']([^\"']*)[\"']",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                "<meta[^>]+content=[\"']([^\"']*)[\"'][^>]+property=[\"']$propiedad[\"']",
                RegexOption.IGNORE_CASE
            )
        )
        for (patron in patrones) {
            patron.find(html)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }

    private fun decodificarHtml(texto: String): String =
        texto
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
}
