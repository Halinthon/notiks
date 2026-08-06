package com.notiks.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun desdeOrigen(o: Origen): String = o.name

    @TypeConverter
    fun aOrigen(s: String): Origen = Origen.valueOf(s)
}

/** Agrega la columna de calificación (0 a 5 estrellas) sin borrar los enlaces ya guardados. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE items ADD COLUMN calificacion INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [Cuaderno::class, Hoja::class, Item::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cuadernoDao(): CuadernoDao
    abstract fun hojaDao(): HojaDao
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notiks.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
