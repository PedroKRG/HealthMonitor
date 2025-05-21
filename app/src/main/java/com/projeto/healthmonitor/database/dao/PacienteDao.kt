package com.projeto.healthmonitor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.projeto.healthmonitor.model.Paciente
import com.projeto.healthmonitor.model.Medico

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

    @Query("SELECT * FROM Paciente WHERE email = :email AND nome = :nome")
    suspend fun buscaPorEmailEnome(email: String, nome: String): Paciente?

    @Query("SELECT * FROM Paciente WHERE medicoId = :medicoId")
    suspend fun listarPacientesDoMedico(medicoId: Long): List<Paciente>

    @Query("UPDATE Paciente SET medicoId = :medicoId WHERE id = :pacienteId")
    suspend fun atribuirMedico(medicoId: Long, pacienteId: Long)

}
