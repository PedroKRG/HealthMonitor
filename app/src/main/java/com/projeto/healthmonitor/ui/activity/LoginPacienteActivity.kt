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

        binding.tvCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroPacienteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configuraBotaoEntrar() {
        binding.activityLoginBotaoEntrar.setOnClickListener {
            val nome = binding.activityLoginPacienteNome.text.toString().trim()
            val senha = binding.activityLoginPacienteSenha.text.toString().trim()
            val email = binding.activityLoginPacienteEmail.text.toString().trim()

            if (nome.isEmpty() || senha.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.i("LoginPaciente", "Tentando login com: $nome | $email | $senha")

            lifecycleScope.launch {
                binding.activityLoginBotaoEntrar.isEnabled = false
                try {
                    val usuarioAutenticado = pacienteDao.autentica(nome, senha, email)

                    if (usuarioAutenticado != null) {
                        Log.i("LoginPaciente", "Usuário autenticado com sucesso: ID ${usuarioAutenticado.id}")

                        val intent = Intent(this@LoginPacienteActivity, TelaPacienteActivity::class.java)
                        intent.putExtra("CHAVE_USUARIO_ID", usuarioAutenticado.id)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        finish()
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
                } finally {
                    binding.activityLoginBotaoEntrar.isEnabled = true
                }
            }
        }
    }
}