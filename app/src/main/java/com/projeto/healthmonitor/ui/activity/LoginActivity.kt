package com.projeto.healthmonitor.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginBinding
import com.projeto.healthmonitor.extensions.vaiPara
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)

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
            val usuario = binding.activityLoginNome.text.toString()
            val senha = binding.activityLoginSenha.text.toString()
            Log.i("LoginActivity", "onCreate: $usuario - $senha")

            lifecycleScope.launch {
                try {
                    val usuarioAutenticado = pacienteDao.autentica(usuario, senha)

                    if (usuarioAutenticado != null) {
                        vaiPara(TelaPacienteActivity::class.java) {
                            putExtra("CHAVE_USUARIO_ID", usuarioAutenticado.id)
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Nome de usuário ou senha incorretos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Erro durante autenticação", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Erro inesperado, tente novamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}