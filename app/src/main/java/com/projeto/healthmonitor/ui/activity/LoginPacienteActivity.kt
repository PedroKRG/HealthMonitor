package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginPacienteBinding
import com.projeto.healthmonitor.extensions.vaiPara
import kotlinx.coroutines.launch


class LoginPacienteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginPacienteBinding.inflate(layoutInflater)
    }

    private val pacienteDao by lazy {
        AppDatabase.instancia(this).pacienteDao()
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

            Log.i("LoginPaciente", "Tentando login com: $nome | $email")

            lifecycleScope.launch {
                try {
                    val usuarioAutenticado = pacienteDao.autentica(nome, senha, email)

                    if (usuarioAutenticado != null) {
                        Log.i("LoginPaciente", "Usuário autenticado com sucesso: ID ${usuarioAutenticado.id}")
                        vaiPara(TelaPacienteActivity::class.java) {
                            putExtra("CHAVE_USUARIO_ID", usuarioAutenticado.id)
                        }
                    } else {
                        Toast.makeText(
                            this@LoginPacienteActivity,
                            "Usuário, senha ou email inválidos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginPaciente", "Erro ao autenticar", e)
                    Toast.makeText(
                        this@LoginPacienteActivity,
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