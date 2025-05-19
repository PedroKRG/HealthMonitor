package com.projeto.healthmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medico(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val email: String,
    val senha: String,
    val crm: String,
    val especialidade: String,
    val dataNascimento: String
)