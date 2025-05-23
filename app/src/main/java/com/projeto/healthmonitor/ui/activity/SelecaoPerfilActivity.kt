package com.projeto.healthmonitor.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.projeto.healthmonitor.databinding.ActivitySelecaoPerfilBinding

class SelecaoPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelecaoPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        verificarPermissaoNotificacao(this)

        super.onCreate(savedInstanceState)


        val sharedPref = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("usuario_id", -1)
        val tipoUsuario = sharedPref.getString("tipo_usuario", null)

        if (usuarioId != -1L && tipoUsuario != null) {
            when (tipoUsuario) {
                "paciente" -> {
                    val intent = Intent(this, TelaPacienteActivity::class.java).apply {
                        putExtra("CHAVE_USUARIO_ID", usuarioId)
                    }
                    startActivity(intent)
                    finish()
                    return
                }

                "medico" -> {
                    val intent = Intent(this, TelaMedicoActivity::class.java).apply {
                        putExtra("CHAVE_USUARIO_ID", usuarioId)
                    }
                    startActivity(intent)
                    finish()
                    return
                }
            }
        }


        binding = ActivitySelecaoPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPaciente.setOnClickListener {
            startActivity(Intent(this, LoginPacienteActivity::class.java))
        }

        binding.btnMedico.setOnClickListener {
            startActivity(Intent(this, LoginMedicoActivity::class.java))
        }
    }

    fun verificarPermissaoNotificacao(context: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
