package com.projeto.healthmonitor.database.dao


import androidx.room.Dao
import androidx.room.Insert

import androidx.room.Query
import com.projeto.healthmonitor.model.RegistroDiario


@Dao
interface RegistroDao {

    @Insert
    suspend fun inserir(registro: RegistroDiario)

    @Query("SELECT * FROM RegistroDiario WHERE pacienteId = :id ORDER BY data ASC")
    suspend fun buscaTodosPorPaciente(id: Long): List<RegistroDiario>
}