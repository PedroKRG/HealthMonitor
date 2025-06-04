package com.projeto.healthmonitor.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projeto.healthmonitor.database.dao.AlertaDao
import com.projeto.healthmonitor.database.dao.MedicoDao
import com.projeto.healthmonitor.database.dao.PacienteDao
import com.projeto.healthmonitor.database.dao.RegistroDao
import com.projeto.healthmonitor.model.Alerta
import com.projeto.healthmonitor.model.Medico
import com.projeto.healthmonitor.model.Paciente
import com.projeto.healthmonitor.model.RegistroDiario


@Database(entities = [Medico::class, Paciente::class, Alerta::class, RegistroDiario::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicoDao(): MedicoDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun alertaDao(): AlertaDao
    abstract fun registroDao(): RegistroDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun instancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "healthmonitor.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
