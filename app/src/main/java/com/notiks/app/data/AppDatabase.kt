package com.notiks.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun desdeOrigen(o: Origen): String = o.name

    @TypeConverter
    fun aOrigen(s: String): Origen = Origen.valueOf(s)
}

@Database(
    entities = [Cuaderno::class, Hoja::class, Item::class],
    version = 1,
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
                ).build().also { INSTANCE = it }
            }
    }
}
