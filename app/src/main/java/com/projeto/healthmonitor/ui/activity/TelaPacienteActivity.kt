package com.projeto.healthmonitor.ui.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.projeto.healthmonitor.model.RegistroDiario


class TelaPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelaPacienteBinding
    private val pacienteDao by lazy { AppDatabase.instancia(this).pacienteDao() }
    private val registroDao by lazy { AppDatabase.instancia(this).registroDao() }

    private var pacienteId: Long = -1L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pacienteId = intent.getLongExtra("CHAVE_USUARIO_ID", -1)
        if (pacienteId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDadosPaciente()
        carregarRegistros()

        binding.btnInserirDados.setOnClickListener {
            val intent = Intent(this, InserirDadosActivity::class.java)
            intent.putExtra("PACIENTE_ID", pacienteId)
            startActivity(intent)
        }

        binding.btnLogOut.setOnClickListener {
            // limpar shared prefs etc
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarDadosPaciente() {
        lifecycleScope.launch {
            val paciente = withContext(Dispatchers.IO) {
                pacienteDao.buscaPorId(pacienteId)
            }
            paciente?.let {
                val dataNascimento = LocalDate.parse(it.dataNascimento)
                val idade = calcularIdade(dataNascimento)
                binding.apply {
                    tvBoasVindas.text = "Olá, ${it.nome}!"
                    tvEmail.text = "Email: ${it.email}"
                    tvIdade.text = "Idade: $idade"
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarRegistros() {
        lifecycleScope.launch {
            val registros = withContext(Dispatchers.IO) {
                registroDao.buscaTodosPorPaciente(pacienteId)
            }
            mostrarGraficos(registros)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularIdade(dataNascimento: LocalDate): Int {
        val hoje = LocalDate.now()
        return Period.between(dataNascimento, hoje).years
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarGraficos(registros: List<RegistroDiario>) {
        val entradasPressao = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.pressaoSistolica.toFloat())
        }

        val entradasGlicemia = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.glicemia.toFloat())
        }

        val dataSetPressao = LineDataSet(entradasPressao, "Pressão Sistólica").apply {
            color = Color.RED
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 4f
            setDrawValues(false)
        }

        val dataSetGlicemia = LineDataSet(entradasGlicemia, "Glicemia").apply {
            color = Color.BLUE
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 4f
            setDrawValues(false)
        }

        binding.chartPressao.data = LineData(dataSetPressao)
        binding.chartPressao.invalidate()

        binding.chartGlicemia.data = LineData(dataSetGlicemia)
        binding.chartGlicemia.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // Recarregar registros sempre que voltar da tela de inserção
        carregarRegistros()
    }
}