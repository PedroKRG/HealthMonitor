package com.projeto.healthmonitor.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
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
    private var medicoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerta)

        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val tipoUsuarioLogado = prefs.getString("tipo_usuario", "") ?: ""
        val usuarioId = prefs.getLong("usuario_id", -1L)

        pacienteId = intent.getLongExtra("pacienteId", -1L)
        medicoId = intent.getLongExtra("medicoId", -1L)

        val destinatario = intent.getStringExtra("destinatario") ?: ""
        val mensagem = intent.getStringExtra("mensagem") ?: "Alerta de saúde"




        if (tipoUsuarioLogado != destinatario) {
            Toast.makeText(this, "Notificação não disponível para este usuário.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (tipoUsuarioLogado == "paciente" && usuarioId != pacienteId) {
            Toast.makeText(this, "Notificação não disponível para este paciente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvMensagem = findViewById(R.id.tvMensagem)
        val btnFechar = findViewById<Button>(R.id.btnFechar)

        tvMensagem.text = mensagem

        if (tipoUsuarioLogado == "paciente") {
            graficoPressao = findViewById(R.id.graficoPressao)
            graficoGlicemia = findViewById(R.id.graficoGlicemia)
            registroDao = AppDatabase.instancia(this).registroDao()

            configurarGraficos()
        } else if (tipoUsuarioLogado == "medico") {

            findViewById<View>(R.id.graficoPressao)?.visibility = View.GONE
            findViewById<View>(R.id.graficoGlicemia)?.visibility = View.GONE

        }

        btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun configurarGraficos() {
        lifecycleScope.launch {
            val registros = withContext(Dispatchers.IO) {
                registroDao.listarRegistrosPorPaciente(pacienteId)
            }

            if (registros.isEmpty()) {
                Toast.makeText(this@AlertaActivity, "Nenhum registro encontrado para o paciente.", Toast.LENGTH_SHORT).show()
                return@launch
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
        xAxis.granularity = 1f

        val yAxis: YAxis = chart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.gridColor = Color.LTGRAY

        chart.invalidate()
    }
}