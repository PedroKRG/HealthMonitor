package com.projeto.healthmonitor.ui.activity


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
import com.projeto.healthmonitor.databinding.ActivityInserirDadosBinding
import com.projeto.healthmonitor.model.RegistroDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class InserirDadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInserirDadosBinding
    private val registroDao by lazy { AppDatabase.instancia(this).registroDao() }

    private var pacienteId: Long = -1L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInserirDadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pacienteId = intent.getLongExtra("PACIENTE_ID", -1)
        if (pacienteId == -1L) {
            Toast.makeText(this, "Paciente inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSalvar.setOnClickListener {
            salvarDados()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun salvarDados() {
        val pressaoStr = binding.edtPressaoSistolica.text.toString()
        val glicemiaStr = binding.edtGlicemia.text.toString()

        if (pressaoStr.isBlank() || glicemiaStr.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val pressao = pressaoStr.toIntOrNull()
        val glicemia = glicemiaStr.toFloatOrNull()

        if (pressao == null || glicemia == null) {
            Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        val registro = RegistroDiario(
            pacienteId = pacienteId,
            data = LocalDate.now().toString(),
            pressaoSistolica = pressao,
            glicemia = glicemia
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                registroDao.inserir(registro)
            }
            Toast.makeText(this@InserirDadosActivity, "Dados salvos!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}