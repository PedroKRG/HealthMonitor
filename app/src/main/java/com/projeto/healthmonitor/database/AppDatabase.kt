package com.projeto.healthmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projeto.healthmonitor.database.dao.PacienteDao
import com.projeto.healthmonitor.model.Paciente


@Database(entities = [Paciente::class],  version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pacienteDao(): PacienteDao

    companion object {
        @Volatile
        private var db: AppDatabase? = null
        fun instancia(context: Context) :AppDatabase {
            return db ?: Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "health.db"
            ).allowMainThreadQueries()
                .build().also { db = it }
        }
    }
}