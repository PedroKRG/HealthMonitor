package com.projeto.healthmonitor.model



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RegistroDiario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pacienteId: Long,
    val data: String,
    val pressaoSistolica: Float,
    val glicemia: Float
)