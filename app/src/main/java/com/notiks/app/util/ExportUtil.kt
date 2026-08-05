package com.notiks.app.util

import com.notiks.app.data.Cuaderno
import com.notiks.app.data.Hoja
import com.notiks.app.data.Item
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializa todo el contenido de Notiks (cuadernos, hojas e ítems) en un único
 * JSON legible, pensado para que el usuario lo guarde en Drive/almacenamiento
 * externo y pueda recuperarlo si formatea el dispositivo.
 */
object ExportUtil {

    fun exportarTodo(
        cuadernos: List<Cuaderno>,
        hojas: List<Hoja>,
        items: List<Item>
    ): String {
        val raiz = JSONObject()
        raiz.put("app", "notiks")
        raiz.put("version", 1)
        raiz.put("exportadoEn", System.currentTimeMillis())

        val arrCuadernos = JSONArray()
        cuadernos.forEach { c ->
            val jc = JSONObject()
            jc.put("id", c.id)
            jc.put("nombre", c.nombre)
            jc.put("colorHex", c.colorHex)
            jc.put("fechaCreacion", c.fechaCreacion)

            val arrHojas = JSONArray()
            hojas.filter { it.cuadernoId == c.id }.forEach { h ->
                val jh = JSONObject()
                jh.put("id", h.id)
                jh.put("titulo", h.titulo)
                jh.put("fechaCreacion", h.fechaCreacion)

                val arrItems = JSONArray()
                items.filter { it.hojaId == h.id }.forEach { i ->
                    val ji = JSONObject()
                    ji.put("url", i.url ?: JSONObject.NULL)
                    ji.put("resumen", i.resumen)
                    ji.put("origen", i.origen.name)
                    ji.put("timestamp", i.timestamp)
                    arrItems.put(ji)
                }
                jh.put("items", arrItems)
                arrHojas.put(jh)
            }
            jc.put("hojas", arrHojas)
            arrCuadernos.put(jc)
        }
        raiz.put("cuadernos", arrCuadernos)
        return raiz.toString(2)
    }
}
