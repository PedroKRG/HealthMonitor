package com.projeto.healthmonitor.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.database.dao.RegistroDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertaActivity : AppCompatActivity() {

    private lateinit var tvMensagem: TextView
    private lateinit var graficoPressao: LineChart
    private lateinit var graficoGlicemia: LineChart
    private lateinit var registroDao: RegistroDao
    private var pacienteId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerta)


        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val tipoUsuario = prefs.getString("tipoUsuario", "") ?: ""
        val usuarioId = prefs.getLong("usuarioId", -1L)

        pacienteId = intent.getLongExtra("pacienteId", -1L)
        val mensagem = intent.getStringExtra("mensagem") ?: "Alerta de saúde"


        if (tipoUsuario == "paciente" && usuarioId != pacienteId) {
            Toast.makeText(this, "Notificação não disponível para este paciente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }



        tvMensagem = findViewById(R.id.tvMensagem)
        graficoPressao = findViewById(R.id.graficoPressao)
        graficoGlicemia = findViewById(R.id.graficoGlicemia)
        val btnFechar = findViewById<Button>(R.id.btnFechar)

        if (pacienteId == -1L) {
            Toast.makeText(this, "Paciente não identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvMensagem.text = mensagem

        registroDao = AppDatabase.instancia(this).registroDao()

        configurarGraficos()

        btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun configurarGraficos() {
        lifecycleScope.launch {
            val registros = withContext(Dispatchers.IO) {
                registroDao.listarRegistrosPorPaciente(pacienteId)
            }

            val registrosOrdenados = registros.sortedBy { it.data }

            val entriesPressao = registrosOrdenados.mapIndexed { index, registro ->
                Entry(index.toFloat(), registro.pressaoSistolica.toFloat())
            }

            val entriesGlicemia = registrosOrdenados.mapIndexed { index, registro ->
                Entry(index.toFloat(), registro.glicemia.toFloat())
            }

            val dataSetPressao = LineDataSet(entriesPressao, "Pressão Sistólica").apply {
                color = Color.RED
                valueTextColor = Color.DKGRAY
                setCircleColor(Color.GRAY)
                lineWidth = 2f
            }

            val dataSetGlicemia = LineDataSet(entriesGlicemia, "Glicemia").apply {
                color = Color.BLUE
                valueTextColor = Color.parseColor("#388E3C")
                setCircleColor(Color.parseColor("#388E3C"))
                lineWidth = 2f
            }

            graficoPressao.data = LineData(dataSetPressao)
            graficoGlicemia.data = LineData(dataSetGlicemia)

            estilizarGrafico(graficoPressao)
            estilizarGrafico(graficoGlicemia)
        }
    }

    private fun estilizarGrafico(chart: LineChart) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)

        chart.axisRight.isEnabled = false

        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val yAxis: YAxis = chart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.gridColor = Color.LTGRAY

        chart.invalidate()
    }
}