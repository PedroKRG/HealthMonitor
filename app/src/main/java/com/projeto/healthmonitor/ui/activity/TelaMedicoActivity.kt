package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.projeto.healthmonitor.Adapter.PacienteAdapter
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityTelaMedicoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTelaMedicoBinding
    private val medicoDao by lazy { AppDatabase.instancia(this).medicoDao() }
    private val pacienteDao by lazy { AppDatabase.instancia(this).pacienteDao() }
    private var medicoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicoId = intent.getLongExtra("CHAVE_USUARIO_ID", -1)

        if (medicoId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do médico", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)

        lifecycleScope.launch {
            val medico = withContext(Dispatchers.IO) {
                medicoDao.buscaPorId(medicoId)
            }

            medico?.let {
                binding.tvBoasVindas.text = "Bem-vindo(a), Dr. ${it.nome}"
                binding.tvCrm.text = "CRM: ${it.crm}"
                binding.tvEspecialidade.text = "Especialidade: ${it.especialidade}"
            }
        }

        binding.btnSair.setOnClickListener { finish() }

        binding.btnLogOut.setOnClickListener {
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, SelecaoPerfilActivity::class.java))
            finish()
        }

        binding.btnVerPacientes.setOnClickListener {
            if (binding.rvPacientes.visibility == View.VISIBLE) {
                binding.rvPacientes.visibility = View.GONE
                binding.btnVerPacientes.text = "Ver Pacientes"
            } else {
                carregarPacientesDoMedico()
                binding.btnVerPacientes.text = "Ocultar Pacientes"
            }
        }

        binding.btnAtribuirPaciente.setOnClickListener {
            val email = binding.etBuscarPaciente.text.toString().trim()
            if (email.isNotBlank()) {
                lifecycleScope.launch {
                    val paciente = withContext(Dispatchers.IO) {
                        pacienteDao.buscaPorEmail(email)
                    }
                    if (paciente != null) {
                        if (paciente.medicoId == medicoId) {
                            Toast.makeText(
                                this@TelaMedicoActivity,
                                "Este paciente já está atribuído a você",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            withContext(Dispatchers.IO) {
                                pacienteDao.atribuirMedico(medicoId, paciente.id)
                            }
                            Toast.makeText(
                                this@TelaMedicoActivity,
                                "Paciente atribuído com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.etBuscarPaciente.text.clear()
                            carregarPacientesDoMedico()
                        }
                    } else {
                        Toast.makeText(
                            this@TelaMedicoActivity,
                            "Paciente não encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor, insira o email do paciente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun carregarPacientesDoMedico() {
        lifecycleScope.launch {
            val pacientes = withContext(Dispatchers.IO) {
                pacienteDao.listarPacientesDoMedico(medicoId)
            }

            if (pacientes.isNotEmpty()) {
                binding.rvPacientes.visibility = View.VISIBLE
                binding.rvPacientes.layoutManager = LinearLayoutManager(this@TelaMedicoActivity)
                binding.rvPacientes.adapter = PacienteAdapter(pacientes) { paciente ->

                    val intent = Intent(this@TelaMedicoActivity, TelaPacienteVisualizacaoActivity::class.java)
                    intent.putExtra("CHAVE_PACIENTE_ID", paciente.id)
                    startActivity(intent)

                }
            } else {
                Toast.makeText(
                    this@TelaMedicoActivity,
                    "Nenhum paciente atribuído ainda",
                    Toast.LENGTH_SHORT
                ).show()
                binding.rvPacientes.visibility = View.GONE
                binding.btnVerPacientes.text = "Ver Pacientes"
            }
        }
    }
}