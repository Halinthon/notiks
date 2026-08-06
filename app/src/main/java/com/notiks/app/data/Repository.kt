package com.notiks.app.data

import android.content.Context

class Repository(context: Context) {
    private val db = AppDatabase.get(context)
    private val cuadernoDao = db.cuadernoDao()
    private val hojaDao = db.hojaDao()
    private val itemDao = db.itemDao()

    fun observarCuadernos() = cuadernoDao.observarTodos()
    suspend fun crearCuaderno(nombre: String, colorHex: String) =
        cuadernoDao.insertar(Cuaderno(nombre = nombre, colorHex = colorHex))
    suspend fun eliminarCuaderno(c: Cuaderno) = cuadernoDao.eliminar(c)

    fun observarHojas(cuadernoId: Long) = hojaDao.observarPorCuaderno(cuadernoId)
    fun observarHojasRecientes() = hojaDao.observarRecientes()
    suspend fun crearHoja(cuadernoId: Long, titulo: String) =
        hojaDao.insertar(Hoja(cuadernoId = cuadernoId, titulo = titulo))
    suspend fun eliminarHoja(h: Hoja) = hojaDao.eliminar(h)
    suspend fun obtenerHoja(id: Long) = hojaDao.obtenerPorId(id)
    suspend fun tocarHoja(hoja: Hoja) =
        hojaDao.actualizar(hoja.copy(fechaUltimaActividad = System.currentTimeMillis()))

    fun observarItems(hojaId: Long) = itemDao.observarPorHoja(hojaId)
    suspend fun guardarItem(hojaId: Long, url: String?, resumen: String, origen: Origen) {
        itemDao.insertar(Item(hojaId = hojaId, url = url, resumen = resumen, origen = origen))
        obtenerHoja(hojaId)?.let { tocarHoja(it) }
    }
    suspend fun eliminarItem(i: Item) = itemDao.eliminar(i)
    suspend fun calificarItem(itemId: Long, calificacion: Int) =
        itemDao.actualizarCalificacion(itemId, calificacion.coerceIn(0, 5))
    suspend fun editarResumen(itemId: Long, resumen: String) =
        itemDao.actualizarResumen(itemId, resumen)
    fun observarConteoTotalItems() = itemDao.observarConteoTotal()
    fun observarConteoPorHoja() = itemDao.observarConteoPorHoja()

    suspend fun obtenerTodoParaExportar(): List<Item> = itemDao.obtenerTodosParaExportar()
    suspend fun obtenerCuadernosLista() = cuadernoDao.observarTodos()

    // ── Importación de un respaldo (mismo dispositivo o uno nuevo) ──
    // Se preservan nombre, color y fechas originales para que el historial
    // se vea igual que en el dispositivo del que salió el respaldo.
    suspend fun importarCuaderno(nombre: String, colorHex: String, fechaCreacion: Long): Long =
        cuadernoDao.insertar(Cuaderno(nombre = nombre, colorHex = colorHex, fechaCreacion = fechaCreacion))

    suspend fun importarHoja(cuadernoId: Long, titulo: String, fechaCreacion: Long, fechaUltimaActividad: Long): Long =
        hojaDao.insertar(
            Hoja(
                cuadernoId = cuadernoId,
                titulo = titulo,
                fechaCreacion = fechaCreacion,
                fechaUltimaActividad = fechaUltimaActividad
            )
        )

    suspend fun importarItem(hojaId: Long, url: String?, resumen: String, origen: Origen, timestamp: Long, calificacion: Int = 0) =
        itemDao.insertar(Item(hojaId = hojaId, url = url, resumen = resumen, origen = origen, timestamp = timestamp, calificacion = calificacion))
}
