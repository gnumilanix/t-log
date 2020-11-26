package com.ignitetech.tlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LogModel::class],
    version = 6,
    exportSchema = false
)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logRepository(): LogDao

    companion object {
        private const val DATABASE = "log"

        @Volatile
        private var instance: LogDatabase? = null

        fun getDatabase(context: Context): LogDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(appContext: Context): LogDatabase {
            return Room.databaseBuilder(appContext, LogDatabase::class.java, DATABASE)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}