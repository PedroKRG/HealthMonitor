package com.projeto.healthmonitor.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.databinding.ActivitySelecaoPerfilBinding

class SelecaoPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelecaoPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
}
