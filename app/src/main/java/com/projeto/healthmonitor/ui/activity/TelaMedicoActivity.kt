package com.projeto.healthmonitor.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityTelaMedicoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelaMedicoBinding
    private val medicoDao by lazy { AppDatabase.instancia(this).medicoDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getLongExtra("CHAVE_USUARIO_ID", -1)

        if (id == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do médico", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            // Usa diretamente o Dispatchers.IO na coroutine para não travar a UI
            val medico = withContext(Dispatchers.IO) {
                medicoDao.buscaPorId(id)
            }

            if (medico != null) {
                binding.tvBoasVindas.text = "Bem-vindo(a), Dr. ${medico.nome}"
                binding.tvCrm.text = "CRM: ${medico.crm}"
                binding.tvEspecialidade.text = "Especialidade: ${medico.especialidade}"
            } else {
                Toast.makeText(this@TelaMedicoActivity, "Médico não encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnSair.setOnClickListener {
            finish()
        }

        binding.btnVerPacientes.setOnClickListener {
            Toast.makeText(this, "Funcionalidade em construção", Toast.LENGTH_SHORT).show()
        }
    }
}