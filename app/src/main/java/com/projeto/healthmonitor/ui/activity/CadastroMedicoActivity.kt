package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.*
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityCadastroMedicoBinding
import com.projeto.healthmonitor.model.Medico
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CadastroMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroMedicoBinding

    private val medicoDao by lazy {
        AppDatabase.instancia(this).medicoDao()
    }

    private var dataNascimento: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelecionarDataNascimento.setOnClickListener {
            exibeDatePicker()
        }

        binding.btnCadastrar.setOnClickListener {
            cadastraMedico()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun exibeDatePicker() {
        val picker = Builder.datePicker()
            .setTitleText("Selecione a data de nascimento")
            .setSelection(todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            dataNascimento = date
            binding.textViewDataSelecionada.text = "Nascimento: $date"
        }

        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun cadastraMedico() {
        val nome = binding.editTextNome.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val senha = binding.editTextSenha.text.toString().trim()
        val crm = binding.editTextCrm.text.toString().trim()
        val especialidade = binding.editTextEspecialidade.text.toString().trim()
        val nascimento = dataNascimento

        if (nome.isBlank() || email.isBlank() || senha.isBlank() || crm.isBlank() || especialidade.isBlank() || nascimento == null) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }


        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Formato de e-mail inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val novoMedico = Medico(
            nome = nome,
            email = email,
            senha = senha,
            crm = crm,
            especialidade = especialidade,
            dataNascimento = nascimento.toString()
        )

        lifecycleScope.launch {
            val existente = medicoDao.buscaPorEmail(email)
            if (existente != null) {
                Toast.makeText(this@CadastroMedicoActivity, "Email já cadastrado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            medicoDao.salva(novoMedico)
            Toast.makeText(this@CadastroMedicoActivity, "Cadastro realizado!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@CadastroMedicoActivity, LoginMedicoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}