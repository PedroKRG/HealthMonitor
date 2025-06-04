package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginPacienteBinding



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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        verificaUsuarioLogado()

        configuraBotaoEntrar()



        binding.tvCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroPacienteActivity::class.java)
            startActivity(intent)
        }
        binding.activityLoginBotaoVoltar.setOnClickListener{
            finish()
        }
    }

    private fun verificaUsuarioLogado() {
        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("usuario_id", -1L)
        val tipoUsuario = sharedPref.getString("tipo_usuario", "")

        if (usuarioId != -1L && tipoUsuario == "paciente") {
            vaiPara(TelaPacienteActivity::class.java) {
                putExtra("CHAVE_USUARIO_ID", usuarioId)
            }
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
                    val usuario = pacienteDao.buscaPorEmailEnome(email, nome)

                    if (usuario != null) {
                        val resultado = BCrypt.verifyer().verify(
                            senha.toCharArray(),
                            usuario.senha
                        )

                        if (resultado.verified) {
                            Log.i(
                                "LoginPaciente",
                                "Usuário autenticado com sucesso: ID ${usuario.id}"
                            )

                            salvarUsuarioLogado(usuario.id, "paciente")

                            val intent =
                                Intent(this@LoginPacienteActivity, TelaPacienteActivity::class.java)
                            intent.putExtra("CHAVE_USUARIO_ID", usuario.id)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginPacienteActivity,
                                "Senha incorreta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginPacienteActivity,
                            "Usuário não encontrado",
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

    private fun salvarUsuarioLogado(usuarioId: Long, tipoUsuario: String) {
        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("usuario_id", usuarioId)
            putString("tipo_usuario", tipoUsuario)
            apply()
        }
    }

    private fun vaiPara(classe: Class<*>, configuracao: Intent.() -> Unit = {}) {
        val intent = Intent(this, classe)
        intent.configuracao()
        startActivity(intent)
    }
}