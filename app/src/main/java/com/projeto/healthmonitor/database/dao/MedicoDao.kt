package com.projeto.healthmonitor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projeto.healthmonitor.model.Medico

@Dao
interface MedicoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(medico: Medico)

    @Query("SELECT * FROM Medico WHERE nome = :nome AND senha = :senha AND email = :email")
    suspend fun autentica(nome: String, senha: String, email: String): Medico?
}