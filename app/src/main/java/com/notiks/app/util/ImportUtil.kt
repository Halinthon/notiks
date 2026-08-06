package com.notiks.app.util

import com.notiks.app.data.Origen
import org.json.JSONArray
import org.json.JSONObject

data class ItemImportado(
    val url: String?,
    val resumen: String,
    val origen: Origen,
    val timestamp: Long,
    val calificacion: Int = 0
)

data class HojaImportada(
    val titulo: String,
    val fechaCreacion: Long,
    val items: List<ItemImportado>
)

data class CuadernoImportado(
    val nombre: String,
    val colorHex: String,
    val fechaCreacion: Long,
    val hojas: List<HojaImportada>
)

/**
 * Lee el .json generado por ExportUtil y lo convierte en una estructura lista
 * para volver a insertar en la base de datos (en este mismo dispositivo o en
 * uno nuevo). Es tolerante a campos faltantes para no romperse si el archivo
 * fue editado a mano o viene de una versión anterior de la app.
 */
object ImportUtil {

    fun parsear(json: String): List<CuadernoImportado> {
        val raiz = JSONObject(json)
        val arrCuadernos = raiz.optJSONArray("cuadernos") ?: JSONArray()
        val resultado = mutableListOf<CuadernoImportado>()

        for (i in 0 until arrCuadernos.length()) {
            val jc = arrCuadernos.getJSONObject(i)
            val nombre = jc.optString("nombre", "Cuaderno importado")
            val colorHex = jc.optString("colorHex", "#6C4DF6")
            val fechaCreacionCuaderno = jc.optLong("fechaCreacion", System.currentTimeMillis())

            val arrHojas = jc.optJSONArray("hojas") ?: JSONArray()
            val hojas = mutableListOf<HojaImportada>()

            for (j in 0 until arrHojas.length()) {
                val jh = arrHojas.getJSONObject(j)
                val titulo = jh.optString("titulo", "Hoja importada")
                val fechaCreacionHoja = jh.optLong("fechaCreacion", System.currentTimeMillis())

                val arrItems = jh.optJSONArray("items") ?: JSONArray()
                val items = mutableListOf<ItemImportado>()

                for (k in 0 until arrItems.length()) {
                    val ji = arrItems.getJSONObject(k)
                    val url = if (ji.isNull("url")) null else ji.optString("url").takeIf { it.isNotBlank() }
                    val resumen = ji.optString("resumen", "")
                    val origen = try {
                        Origen.valueOf(ji.optString("origen", "TEXTO"))
                    } catch (e: IllegalArgumentException) {
                        Origen.TEXTO
                    }
                    val timestamp = ji.optLong("timestamp", System.currentTimeMillis())
                    val calificacion = ji.optInt("calificacion", 0).coerceIn(0, 5)
                    items += ItemImportado(url, resumen, origen, timestamp, calificacion)
                }
                hojas += HojaImportada(titulo, fechaCreacionHoja, items)
            }
            resultado += CuadernoImportado(nombre, colorHex, fechaCreacionCuaderno, hojas)
        }
        return resultado
    }

    fun contarCuadernos(cuadernos: List<CuadernoImportado>): Int = cuadernos.size
    fun contarHojas(cuadernos: List<CuadernoImportado>): Int = cuadernos.sumOf { it.hojas.size }
    fun contarItems(cuadernos: List<CuadernoImportado>): Int =
        cuadernos.sumOf { c -> c.hojas.sumOf { it.items.size } }
}
