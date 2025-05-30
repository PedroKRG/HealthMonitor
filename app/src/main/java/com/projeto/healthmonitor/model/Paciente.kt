
package com.projeto.healthmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity

data class Paciente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val dataNascimento: String,
    val senha: String,
    val email: String,
    val medicoId: Long? = null
)