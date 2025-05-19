package com.projeto.healthmonitor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.projeto.healthmonitor.model.Paciente


@Dao
interface PacienteDao {

    @Insert
    suspend fun salva(paciente: Paciente)
    @Query("""
        SELECT * FROM Paciente 
        WHERE nome = :nome
        AND senha = :senha
        AND email = :email
        """)

    suspend fun autentica(nome: String, senha: String, email: String): Paciente?

    @Query("SELECT * FROM Paciente WHERE id = :id")
    fun buscaPorId(id: Long): Paciente?

    @Query("SELECT * FROM Paciente WHERE email = :email")
    suspend fun buscaPorEmail(email: String): Paciente?

}
