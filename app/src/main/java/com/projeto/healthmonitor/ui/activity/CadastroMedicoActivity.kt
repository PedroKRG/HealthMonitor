package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker.*
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityCadastroMedicoBinding
import com.projeto.healthmonitor.model.Medico
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(binding.root)

        binding.btnSelecionarDataNascimento.setOnClickListener {
            exibeDatePicker()
        }

        binding.btnCadastrar.setOnClickListener {
            cadastraMedico()
        }
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

        binding.contentLayout.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        binding.lottieProgress.playAnimation()

        lifecycleScope.launch {
            val existente = withContext(Dispatchers.IO) {
                medicoDao.buscaPorEmail(email)
            }

            if (existente != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CadastroMedicoActivity, "Email já cadastrado", Toast.LENGTH_SHORT).show()
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.loadingLayout.visibility = View.GONE
                    binding.lottieProgress.cancelAnimation()
                }
                return@launch
            }

            val senhaCriptografada = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                .hashToString(12, senha.toCharArray())

            val novoMedico = Medico(
                nome = nome,
                email = email,
                senha = senhaCriptografada,
                crm = crm,
                especialidade = especialidade,
                dataNascimento = nascimento.toString()
            )

            withContext(Dispatchers.IO) {
                medicoDao.salva(novoMedico)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CadastroMedicoActivity, "Cadastro realizado!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@CadastroMedicoActivity, LoginMedicoActivity::class.java)
                startActivity(intent)
                binding.lottieProgress.cancelAnimation()
                finish()
            }
        }
    }
}