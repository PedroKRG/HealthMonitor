package com.projeto.healthmonitor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projeto.healthmonitor.model.Medico
import com.projeto.healthmonitor.model.Paciente

@Dao
interface MedicoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(medico: Medico)

    @Query("SELECT * FROM Medico WHERE nome = :nome AND senha = :senha AND email = :email")
    suspend fun autentica(nome: String, senha: String, email: String): Medico?

    @Query("SELECT * FROM Medico WHERE id = :id")
    suspend fun buscaPorId(id: Long): Medico?

    @Query("SELECT * FROM medico WHERE email = :email")
    suspend fun buscaPorEmail(email: String): Medico?
}