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
        val maxTentativas = 3

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
                kotlinx.coroutines.delay(350)
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

            // Parar animação e fechar Activity
            binding.lottieProgress.cancelAnimation()
            finish()
        }
    }
}