package com.notiks.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CuadernoDao {
    @Query("SELECT * FROM cuadernos ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<Cuaderno>>

    @Insert
    suspend fun insertar(cuaderno: Cuaderno): Long

    @Update
    suspend fun actualizar(cuaderno: Cuaderno)

    @Delete
    suspend fun eliminar(cuaderno: Cuaderno)
}

@Dao
interface HojaDao {
    @Query("SELECT * FROM hojas WHERE cuadernoId = :cuadernoId ORDER BY fechaUltimaActividad DESC")
    fun observarPorCuaderno(cuadernoId: Long): Flow<List<Hoja>>

    @Query("SELECT * FROM hojas ORDER BY fechaUltimaActividad DESC")
    fun observarRecientes(): Flow<List<Hoja>>

    @Insert
    suspend fun insertar(hoja: Hoja): Long

    @Update
    suspend fun actualizar(hoja: Hoja)

    @Delete
    suspend fun eliminar(hoja: Hoja)

    @Query("SELECT * FROM hojas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Hoja?
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE hojaId = :hojaId ORDER BY timestamp ASC")
    fun observarPorHoja(hojaId: Long): Flow<List<Item>>

    @Insert
    suspend fun insertar(item: Item): Long

    @Delete
    suspend fun eliminar(item: Item)

    @Query("SELECT * FROM items ORDER BY timestamp ASC")
    suspend fun obtenerTodosParaExportar(): List<Item>

    @Query("UPDATE items SET calificacion = :calificacion WHERE id = :itemId")
    suspend fun actualizarCalificacion(itemId: Long, calificacion: Int)

    @Query("UPDATE items SET resumen = :resumen WHERE id = :itemId")
    suspend fun actualizarResumen(itemId: Long, resumen: String)

    @Query("SELECT COUNT(*) FROM items")
    fun observarConteoTotal(): Flow<Int>
}
