package com.uvg.lab09_cafedeespecialidad.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.uvg.lab09_cafedeespecialidad.data.local.entity.FavoriteEntity
import com.uvg.lab09_cafedeespecialidad.data.local.entity.OrderLineEntity

@Database(
    entities = [
        FavoriteEntity::class,
        OrderLineEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao

    companion object {

        private const val DATABASE_NAME = "store_database.db"

        @Volatile
        private var instance: StoreDatabase? = null

        fun getInstance(context: Context): StoreDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(
                    context = context.applicationContext
                ).also { database ->
                    instance = database
                }
            }
        }

        private fun createDatabase(
            context: Context
        ): StoreDatabase {
            return Room.databaseBuilder<StoreDatabase>(
                context = context,
                name = DATABASE_NAME
            )
                .setDriver(AndroidSQLiteDriver())
                .build()
        }
    }
}