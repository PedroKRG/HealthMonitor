package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityLoginMedicoBinding
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt
import com.projeto.healthmonitor.extensions.Notificacoes.criarCanais
import com.projeto.healthmonitor.extensions.Notificacoes.enviarNotificacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        verificaUsuarioLogado()

        configuraBotaoEntrar()


        binding.tvCadastrarMedico.setOnClickListener {
            startActivity(Intent(this, CadastroMedicoActivity::class.java))
        }

        binding.activityLoginBotaoVoltar.setOnClickListener {
            finish()
        }
    }

    private fun verificaUsuarioLogado() {
        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("usuario_id", -1L)
        val tipoUsuario = sharedPref.getString("tipo_usuario", "")

        if (usuarioId != -1L && tipoUsuario == "medico") {
            Log.d("LoginMedico", "Usuário já logado: ID=$usuarioId")
            vaiParaTelaMedico(usuarioId)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
                    val usuario = medicoDao.buscaPorEmailENome(email, nome)

                    if (usuario != null) {
                        val resultado = BCrypt.verifyer().verify(senha.toCharArray(), usuario.senha)

                        if (resultado.verified) {
                            Log.i("LoginMedico", "Usuário autenticado: ${usuario.id}")

                            salvarUsuarioLogado(usuario.id, "medico")
                            criarCanais(this@LoginMedicoActivity)


                            val alertaDao = AppDatabase.instancia(this@LoginMedicoActivity).alertaDao()
                            val alertaAtivo = withContext(Dispatchers.IO) {
                                alertaDao.buscarAlertaAtivoPorMedico(usuario.id)
                            }

                            if (alertaAtivo != null) {
                                enviarNotificacao(
                                    context = this@LoginMedicoActivity,
                                    titulo = "Alerta de Saúde",
                                    mensagem = alertaAtivo.mensagem ?: "Você tem um novo alerta de saúde.",
                                    activityDestino = AlertaActivity::class.java,
                                    pacienteId = alertaAtivo.pacienteId ?: -1L,
                                    destinatario = "medico"
                                )
                            } else {
                                enviarNotificacao(
                                    context = this@LoginMedicoActivity,
                                    titulo = "Bem-vindo, Médico!",
                                    mensagem = "Login realizado com sucesso.",
                                    activityDestino = TelaMedicoActivity::class.java,
                                    pacienteId = -1L,
                                    destinatario = "medico"
                                )
                            }

                            vaiParaTelaMedico(usuario.id)

                        } else {
                            Toast.makeText(
                                this@LoginMedicoActivity,
                                "Senha incorreta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginMedicoActivity,
                            "Usuário não encontrado",
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

    private fun salvarUsuarioLogado(usuarioId: Long, tipoUsuario: String) {
        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("usuario_id", usuarioId)
            putString("tipo_usuario", tipoUsuario)
            apply()
        }
        Log.d("LoginMedico", "Usuário salvo nas SharedPreferences: ID=$usuarioId, Tipo=$tipoUsuario")
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
