package com.projeto.healthmonitor.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityTelaPacienteVisualizacaoBinding
import com.projeto.healthmonitor.extensions.DateValueFormatter
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
        if (registros.isEmpty()) {
            Toast.makeText(this, "Nenhum dado disponível para exibir os gráficos", Toast.LENGTH_SHORT).show()

            binding.chartPressao.visibility = View.GONE
            binding.chartGlicemia.visibility = View.GONE


            return
        }

        binding.chartPressao.visibility = View.VISIBLE
        binding.chartGlicemia.visibility = View.VISIBLE


        val entradasPressao = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.pressaoSistolica.toFloat())
        }

        val entradasGlicemia = registros.mapIndexed { index, registro ->
            Entry(index.toFloat(), registro.glicemia.toFloat())
        }

        val dataSetPressao = LineDataSet(entradasPressao, "Pressão Sistólica").apply {
            color = Color.RED
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 6f
            setDrawValues(true)
        }

        val dataSetGlicemia = LineDataSet(entradasGlicemia, "Glicemia").apply {
            color = Color.BLUE
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 6f
            setDrawValues(true)
        }


        val dateFormatter = DateValueFormatter(registros)


        binding.chartPressao.xAxis.apply {
            valueFormatter = dateFormatter
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = -30f
        }


        binding.chartGlicemia.xAxis.apply {
            valueFormatter = dateFormatter
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = -30f
        }

        binding.chartPressao.data = LineData(dataSetPressao)
        binding.chartPressao.invalidate()

        binding.chartGlicemia.data = LineData(dataSetGlicemia)
        binding.chartGlicemia.invalidate()
    }
}