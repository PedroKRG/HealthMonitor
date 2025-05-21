package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityTelaPacienteVisualizacaoBinding
import com.projeto.healthmonitor.model.RegistroDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaPacienteVisualizacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelaPacienteVisualizacaoBinding
    private val registroDao by lazy { AppDatabase.instancia(this).registroDao() }
    private val pacienteDao by lazy { AppDatabase.instancia(this).pacienteDao() }
    private var pacienteId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaPacienteVisualizacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pacienteId = intent.getLongExtra("CHAVE_PACIENTE_ID", -1)
        if (pacienteId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDadosPaciente()
        carregarRegistrosDoPaciente()

        binding.btnVoltar.setOnClickListener{
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun carregarDadosPaciente() {
        lifecycleScope.launch {
            val paciente = withContext(Dispatchers.IO) {
                pacienteDao.buscaPorId(pacienteId)
            }


            paciente?.let {
                binding.textView.text = "Dados do paciente: ${it.nome}"
            }
        }
    }

    private fun carregarRegistrosDoPaciente() {
        lifecycleScope.launch {
            val registros = withContext(Dispatchers.IO) {
                registroDao.listarRegistrosPorPaciente(pacienteId)
            }

            if (registros.isEmpty()) {
                Toast.makeText(
                    this@TelaPacienteVisualizacaoActivity,
                    "Nenhum registro encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                mostrarGraficos(registros)
            }
        }
    }

    private fun mostrarGraficos(registros: List<RegistroDiario>) {
        val entradasPressao = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.pressaoSistolica.toFloat())
        }

        val entradasGlicemia = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.glicemia.toFloat())
        }

        val dataSetPressao = LineDataSet(entradasPressao, "Pressão").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
        }

        val dataSetGlicemia = LineDataSet(entradasGlicemia, "Glicemia").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.RED)
        }

        binding.chartPressao.apply {
            data = LineData(dataSetPressao)
            visibility = View.VISIBLE
            invalidate()
        }

        binding.chartGlicemia.apply {
            data = LineData(dataSetGlicemia)
            visibility = View.VISIBLE
            invalidate()
        }
    }
}