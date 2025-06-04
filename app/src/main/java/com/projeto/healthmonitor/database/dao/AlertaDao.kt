package com.projeto.healthmonitor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.projeto.healthmonitor.model.Alerta

@Dao
interface AlertaDao {

    @Query("SELECT * FROM Alerta WHERE medicoId = :medicoId AND ativo = 1 LIMIT 1")
    suspend fun buscarAlertaAtivoPorMedico(medicoId: Long): Alerta?

    @Query("SELECT * FROM Alerta WHERE medicoId = :medicoId AND ativo = 1")
    suspend fun buscarTodosAlertasAtivosPorMedico(medicoId: Long): List<Alerta>

    @Insert
    suspend fun inserirAlerta(alerta: Alerta)

    @Update
    suspend fun atualizarAlerta(alerta: Alerta)

    @Query("UPDATE Alerta SET ativo = 0 WHERE id = :alertaId")
    suspend fun desativarAlerta(alertaId: Long)

    @Query("SELECT * FROM Alerta WHERE medicoId = :medicoId AND ativo = 1 ORDER BY id DESC")
    suspend fun buscarAlertasAtivosPorMedico(medicoId: Long): List<Alerta>
}