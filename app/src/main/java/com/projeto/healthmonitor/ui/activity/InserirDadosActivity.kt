package com.projeto.healthmonitor.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.projeto.healthmonitor.api.TimeApiService
import com.projeto.healthmonitor.database.AppDatabase
import com.projeto.healthmonitor.databinding.ActivityInserirDadosBinding
import com.projeto.healthmonitor.extensions.Notificacoes
import com.projeto.healthmonitor.model.RegistroDiario
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class InserirDadosActivity : AppCompatActivity() {

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://worldtimeapi.org/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val timeApi = retrofit.create(TimeApiService::class.java)

    private lateinit var binding: ActivityInserirDadosBinding
    private val registroDao by lazy { AppDatabase.instancia(this).registroDao() }

    private var pacienteId: Long = -1L

    suspend fun obterDataAtual(): String {
        var tentativa = 0
        val maxTentativas = 50

        while (tentativa < maxTentativas) {
            try {
                val resposta = timeApi.getCurrentTime()
                return formatarData(resposta.datetime)
            } catch (e: Exception) {
                tentativa++
                Log.e(
                    "TimeApi",
                    "Erro ao obter data atual na tentativa $tentativa: ${e.message}",
                    e
                )
                if (tentativa == maxTentativas) {
                    break
                }
                kotlinx.coroutines.delay(100)
            }
        }
        return "00/00/0000"
    }

    private fun formatarData(dataCompleta: String): String {
        val dataBruta = dataCompleta.substring(0, 10) // "2025-05-22"
        return dataBruta.split("-").reversed().joinToString("/") // "22/05/2025"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        Notificacoes.criarCanal(this)
        super.onCreate(savedInstanceState)
        binding = ActivityInserirDadosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pacienteId = intent.getLongExtra("PACIENTE_ID", -1)
        if (pacienteId == -1L) {
            Toast.makeText(this, "Paciente inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSalvar.setOnClickListener {
            salvarDados()
        }

        binding.btnVoltar.setOnClickListener {
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
    private fun salvarDados() {
        val pressaoStr = binding.edtPressaoSistolica.text.toString()
        val glicemiaStr = binding.edtGlicemia.text.toString()

        if (pressaoStr.isBlank() || glicemiaStr.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val pressao = pressaoStr.toIntOrNull()
        val glicemia = glicemiaStr.toIntOrNull()

        if (pressao == null || glicemia == null) {
            Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show()
            return
        }


        binding.contentLayout.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        binding.lottieProgress.playAnimation()


        lifecycleScope.launch {
            val dataAtual = withContext(Dispatchers.IO) {
                obterDataAtual()
            }

            if (dataAtual == "00/00/0000") {

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@InserirDadosActivity,
                        "Não foi possível salvar os dados, Tente novamente mais tarde.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.loadingLayout.visibility = View.GONE
                    binding.lottieProgress.cancelAnimation()
                }
                return@launch
            }

            val registro = RegistroDiario(
                pacienteId = pacienteId,
                data = dataAtual,
                pressaoSistolica = pressao,
                glicemia = glicemia
            )

            withContext(Dispatchers.IO) {
                registroDao.inserir(registro)
            }

            Toast.makeText(this@InserirDadosActivity, "Dados salvos!", Toast.LENGTH_SHORT).show()


            binding.lottieProgress.cancelAnimation()
            avaliarEEnviarNotificacao(pressao, glicemia, pacienteId)
            finish()
        }
    }

    private fun avaliarEEnviarNotificacao(pressao: Int, glicemia: Int, pacienteId: Long) {
        val mensagens = mutableListOf<String>()


        when {
            pressao < 90 -> mensagens.add("Pressão muito baixa: $pressao mmHg")
            pressao in 90..120 -> mensagens.add("Pressão normal: $pressao mmHg")
            pressao in 121..139 -> mensagens.add("Pressão ligeiramente elevada: $pressao mmHg")
            pressao >= 140 -> mensagens.add("Pressão alta: $pressao mmHg")
        }


        when {
            glicemia < 70 -> mensagens.add("Glicemia muito baixa: $glicemia mg/dL")
            glicemia in 70..99 -> mensagens.add("Glicemia normal: $glicemia mg/dL")
            glicemia in 100..125 -> mensagens.add("Glicemia preocupante: $glicemia mg/dL")
            glicemia >= 126 -> mensagens.add("Glicemia alta: $glicemia mg/dL")
        }

        if (mensagens.isNotEmpty()) {

            Notificacoes.enviarNotificacao(
                context = this,
                titulo = "Análise dos Dados Registrados",
                mensagem = mensagens.joinToString("\n"),
                id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                activityDestino = AlertaActivity::class.java,
                pacienteId = pacienteId
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val medico = registroDao.buscarMedicoPorPaciente(pacienteId)
                val paciente = registroDao.buscarPacientePorId(pacienteId)

                if (medico != null && paciente != null) {
                    val nomePaciente = paciente.nome

                    withContext(Dispatchers.Main) {
                        // Notificação para o médico
                        Notificacoes.enviarNotificacao(
                            context = this@InserirDadosActivity,
                            titulo = "Alerta de Saúde: $nomePaciente",
                            mensagem = mensagens.joinToString("\n"),
                            id = ((System.currentTimeMillis() + 1) % Int.MAX_VALUE).toInt(),
                            activityDestino = AlertaActivity::class.java,
                            pacienteId = medico.id
                        )
                    }
                }
            }
        }
    }
}