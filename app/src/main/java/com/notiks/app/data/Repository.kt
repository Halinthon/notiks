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

    suspend fun obtenerTodoParaExportar(): List<Item> = itemDao.obtenerTodosParaExportar()
    suspend fun obtenerCuadernosLista() = cuadernoDao.observarTodos()
}
