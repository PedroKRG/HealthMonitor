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
        binding = ActivitySelecaoPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPaciente.setOnClickListener {
            val intent = Intent(this, LoginPacienteActivity::class.java)
            startActivity(intent)
        }

        binding.btnMedico.setOnClickListener {
            val intent = Intent(this, LoginMedicoActivity::class.java)
            startActivity(intent)
        }
    }

}