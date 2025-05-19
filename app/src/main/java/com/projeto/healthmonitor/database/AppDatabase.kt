package com.projeto.healthmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projeto.healthmonitor.database.dao.MedicoDao
import com.projeto.healthmonitor.database.dao.PacienteDao
import com.projeto.healthmonitor.model.Medico
import com.projeto.healthmonitor.model.Paciente


@Database(entities = [Paciente::class, Medico::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicoDao(): MedicoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun instancia(context: Context): AppDatabase {
            return instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "healthmonitor.db"
            ).build().also { instancia = it }
        }
    }
}