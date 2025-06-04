package com.projeto.healthmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alerta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicoId: Long,
    val pacienteId: Long?,
    val mensagem: String?,
    val ativo: Boolean
)