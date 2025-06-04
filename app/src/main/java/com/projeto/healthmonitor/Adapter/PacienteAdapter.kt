package com.projeto.healthmonitor.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.model.Paciente

class PacienteAdapter(
    private val pacientes: List<Paciente>,
    private val onClick: (Paciente) -> Unit
) : RecyclerView.Adapter<PacienteAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome = itemView.findViewById<TextView>(R.id.tvNomePaciente)

        fun bind(paciente: Paciente) {
            nome.text = paciente.nome
            itemView.setOnClickListener { onClick(paciente) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paciente, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = pacientes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pacientes[position])
    }
}