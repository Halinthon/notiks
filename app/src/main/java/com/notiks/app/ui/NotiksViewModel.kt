package com.notiks.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notiks.app.data.Cuaderno
import com.notiks.app.data.Origen
import com.notiks.app.data.Repository
import com.notiks.app.util.ExportUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotiksViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    val cuadernos = repo.observarCuadernos()
    val hojasRecientes = repo.observarHojasRecientes()

    fun hojasDe(cuadernoId: Long) = repo.observarHojas(cuadernoId)
    fun itemsDe(hojaId: Long) = repo.observarItems(hojaId)

    fun crearCuaderno(nombre: String, colorHex: String = "#6C4DF6") {
        viewModelScope.launch { repo.crearCuaderno(nombre, colorHex) }
    }

    fun eliminarCuaderno(c: Cuaderno) {
        viewModelScope.launch { repo.eliminarCuaderno(c) }
    }

    fun crearHoja(cuadernoId: Long, titulo: String, onCreada: (Long) -> Unit = {}) {
        viewModelScope.launch { onCreada(repo.crearHoja(cuadernoId, titulo)) }
    }

    fun guardarItem(hojaId: Long, url: String?, resumen: String, origen: Origen) {
        viewModelScope.launch { repo.guardarItem(hojaId, url, resumen, origen) }
    }

    suspend fun exportarJson(): String {
        val listaC = repo.obtenerCuadernosLista().first()
        val hojas = mutableListOf<com.notiks.app.data.Hoja>()
        listaC.forEach { c ->
            hojas += repo.observarHojas(c.id).first()
        }
        val items = repo.obtenerTodoParaExportar()
        return ExportUtil.exportarTodo(listaC, hojas, items)
    }
}
