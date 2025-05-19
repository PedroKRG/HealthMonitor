package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginMedicoBinding
import com.projeto.healthmonitor.extensions.vaiPara
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


        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("usuario_id", -1)
        val tipoUsuario = sharedPref.getString("tipo_usuario", "")

        if (usuarioId != -1L && tipoUsuario == "medico") {
            vaiParaTelaMedico(usuarioId)
            return
        }

        configuraBotaoEntrar()

        binding.tvCadastrarMedico.setOnClickListener {
            val intent = Intent(this, CadastroMedicoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configuraBotaoEntrar() {
        binding.activityLoginMedicoBotaoEntrar.setOnClickListener {
            val nome = binding.activityLoginMedicoNome.text.toString().trim()
            val senha = binding.activityLoginMedicoSenha.text.toString().trim()
            val email = binding.activityLoginMedicoEmail.text.toString().trim()

            if (nome.isEmpty() || senha.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.i("LoginMedico", "Tentando login com: $nome | $email")

            lifecycleScope.launch {
                binding.activityLoginMedicoBotaoEntrar.isEnabled = false

                try {
                    val usuarioAutenticado = medicoDao.autentica(nome, senha, email)

                    if (usuarioAutenticado != null) {
                        Log.i("LoginMedico", "Usuário autenticado: ${usuarioAutenticado.id}")


                        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                        sharedPref.edit()
                            .putLong("usuario_id", usuarioAutenticado.id)
                            .putString("tipo_usuario", "medico")
                            .apply()

                        vaiParaTelaMedico(usuarioAutenticado.id)
                    } else {
                        Toast.makeText(
                            this@LoginMedicoActivity,
                            "Usuário, senha ou email inválidos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginMedico", "Erro ao autenticar", e)
                    Toast.makeText(
                        this@LoginMedicoActivity,
                        "Erro inesperado. Tente novamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    binding.activityLoginMedicoBotaoEntrar.isEnabled = true
                }
            }
        }
    }

    private fun vaiParaTelaMedico(id: Long) {
        val intent = Intent(this, TelaMedicoActivity::class.java).apply {
            putExtra("CHAVE_USUARIO_ID", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}