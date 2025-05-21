package com.projeto.healthmonitor.database.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy

import androidx.room.Query
import androidx.room.Update
import com.projeto.healthmonitor.model.RegistroDiario


@Dao
interface RegistroDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(registro: RegistroDiario)

    @Query("SELECT * FROM RegistroDiario WHERE pacienteId = :id ORDER BY data ASC")
    suspend fun buscaTodosPorPaciente(id: Long): List<RegistroDiario>

    @Query("SELECT * FROM RegistroDiario WHERE pacienteId = :pacienteId ORDER BY data DESC")
    suspend fun listarRegistrosPorPaciente(pacienteId: Long): List<RegistroDiario>

    @Update
    suspend fun atualizar(registro: RegistroDiario)

    @Delete
    suspend fun deletar(registro: RegistroDiario)
}
