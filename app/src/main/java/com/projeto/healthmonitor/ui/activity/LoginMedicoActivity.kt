package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginMedicoBinding
import kotlinx.coroutines.launch

class LoginMedicoActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginMedicoBinding.inflate(layoutInflater)
    }

    private val medicoDao by lazy {
        AppDatabase.instancia(this).medicoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        configuraBotaoEntrar()
    }

    private fun configuraBotaoEntrar() {
        binding.activityLoginBotaoEntrar.setOnClickListener {
            val nome = binding.activityLoginNome.text.toString().trim()
            val senha = binding.activityLoginSenha.text.toString().trim()
            val email = binding.activityLoginEmail.text.toString().trim()

            if (nome.isEmpty() || senha.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.i("LoginMedico", "Tentando login com: $nome | $email")

            lifecycleScope.launch {
                try {
                    val medicoAutenticado = medicoDao.autentica(nome, senha, email)

                    if (medicoAutenticado != null) {
                        Log.i("LoginMedico", "Médico autenticado com sucesso: ID ${medicoAutenticado.id}")
                        vaiPara(TelaMedicoActivity::class.java) {
                            putExtra("CHAVE_USUARIO_ID", medicoAutenticado.id)
                        }
                    } else {
                        Toast.makeText(
                            this@LoginMedicoActivity,
                            "Nome, senha ou email inválidos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginMedico", "Erro ao autenticar", e)
                    Toast.makeText(
                        this@LoginMedicoActivity,
                        "Erro inesperado. Tente novamente mais tarde.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun vaiPara(classe: Class<*>, configuracao: Intent.() -> Unit = {}) {
        val intent = Intent(this, classe)
        intent.configuracao()
        startActivity(intent)
    }
}