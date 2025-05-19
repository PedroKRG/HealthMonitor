package com.projeto.healthmonitor.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityTelaPacienteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Period

class TelaPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelaPacienteBinding
    private val pacienteDao by lazy { AppDatabase.instancia(this).pacienteDao() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getLongExtra("CHAVE_USUARIO_ID", -1)
        if (id == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)

        lifecycleScope.launch {
            val paciente = withContext(Dispatchers.IO) {
                pacienteDao.buscaPorId(id)
            }

            if (paciente != null) {
                sharedPref.edit()
                    .putLong("usuario_id", paciente.id)
                    .putString("usuario_nome", paciente.nome)
                    .putString("usuario_email", paciente.email)
                    .apply()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dataNascimento = LocalDate.parse(paciente.dataNascimento)
                    val idadeCalculada = calcularIdade(dataNascimento)

                    binding.apply {
                        tvBoasVindas.text = "Olá, ${paciente.nome}!"
                        tvEmail.text = "Email: ${paciente.email}"
                        tvIdade.text = "Idade: $idadeCalculada"
                    }
                } else {
                    Toast.makeText(
                        this@TelaPacienteActivity,
                        "Seu dispositivo não suporta cálculo de idade (API < 26)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@TelaPacienteActivity,
                    "Paciente não encontrado",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        binding.btnLogOut.setOnClickListener {
            sharedPref.edit().clear().apply()

            val intent = Intent(this, SelecaoPerfilActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSair.setOnClickListener {
            finish()
        }

        binding.btnVerHistorico.setOnClickListener {
            Toast.makeText(this, "Funcionalidade em construção", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularIdade(dataNascimento: LocalDate): Int {
        val hoje = LocalDate.now()
        return Period.between(dataNascimento, hoje).years
    }
}
