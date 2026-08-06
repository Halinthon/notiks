package com.notiks.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Un cuaderno agrupa varias Hojas por tema, ej: "Trabajo", "Recetas". */
@Entity(tableName = "cuadernos")
data class Cuaderno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val colorHex: String = "#6750A4",
    val fechaCreacion: Long = System.currentTimeMillis()
)

/** Una Hoja es como una "sesión de chat": una lista cronológica de ítems guardados. */
@Entity(
    tableName = "hojas",
    foreignKeys = [ForeignKey(
        entity = Cuaderno::class,
        parentColumns = ["id"],
        childColumns = ["cuadernoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Hoja(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuadernoId: Long,
    val titulo: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaUltimaActividad: Long = System.currentTimeMillis()
)

enum class Origen { YOUTUBE, X, INSTAGRAM, DISCOVER, WEB, TEXTO }

/** Un ítem guardado dentro de una Hoja: un enlace o una nota de texto. */
@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(
        entity = Hoja::class,
        parentColumns = ["id"],
        childColumns = ["hojaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hojaId: Long,
    val url: String?,
    /** Resumen escrito por el usuario, máximo ~30 palabras. */
    val resumen: String,
    val origen: Origen,
    val timestamp: Long = System.currentTimeMillis(),
    /** Calificación de 0 (sin calificar) a 5 estrellas, según qué tanto le interesó al usuario. */
    val calificacion: Int = 0
)
