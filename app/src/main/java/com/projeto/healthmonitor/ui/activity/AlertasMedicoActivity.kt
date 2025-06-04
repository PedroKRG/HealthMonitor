package com.projeto.healthmonitor.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.projeto.healthmonitor.Adapter.AlertaAdapter
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityAlertasMedicoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertasMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertasMedicoBinding
    private val alertaDao by lazy { AppDatabase.instancia(this).alertaDao() }

    private var medicoId: Long = -1L
    private var alertas = mutableListOf<com.projeto.healthmonitor.model.Alerta>()
    private lateinit var alertaAdapter: AlertaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertasMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicoId = intent.getLongExtra("MEDICO_ID", -1L)
        if (medicoId == -1L) {
            Toast.makeText(this, "Erro ao carregar alertas", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.btnVoltar.setOnClickListener {
            finish()
        }

        alertaAdapter = AlertaAdapter(alertas) { alerta ->
            lifecycleScope.launch(Dispatchers.IO) {
                alertaDao.desativarAlerta(alerta.id)
                carregarAlertas() // recarrega a lista após desativar
            }
            runOnUiThread {
                Toast.makeText(this, "Alerta visualizado e desativado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvAlertas.layoutManager = LinearLayoutManager(this)
        binding.rvAlertas.adapter = alertaAdapter

        carregarAlertas()
    }

    private fun carregarAlertas() {
        lifecycleScope.launch {
            val lista = withContext(Dispatchers.IO) {
                alertaDao.buscarAlertasAtivosPorMedico(medicoId)
            }

            alertas.clear()
            alertas.addAll(lista)
            alertaAdapter.notifyDataSetChanged()

            if (alertas.isEmpty()) {
                Toast.makeText(this@AlertasMedicoActivity, "Nenhum alerta ativo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}