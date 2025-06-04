package com.projeto.healthmonitor.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projeto.healthmonitor.R
import com.projeto.healthmonitor.model.Alerta

class AlertaAdapter(
    private val alertas: List<Alerta>,
    private val onClick: (Alerta) -> Unit
) : RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder>() {

    class AlertaViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alerta, parent, false)
        return AlertaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        val alerta = alertas[position]
        val mensagem = holder.view.findViewById<TextView>(R.id.tvMensagem)
        val layout = holder.view.findViewById<LinearLayout>(R.id.alertaLayout)

        mensagem.text = alerta.mensagem
        mensagem.contentDescription = "Mensagem de alerta: ${alerta.mensagem}"


        layout.setBackgroundColor(
            when {
                alerta.mensagem?.contains("muito", true) == true || alerta.mensagem?.contains("alta", true) == true ->
                    Color.parseColor("#FFCDD2") // vermelho claro
                alerta.mensagem?.contains("ligeiramente", true) == true || alerta.mensagem?.contains("preocupante", true) == true ->
                    Color.parseColor("#FFF9C4") // amarelo claro
                else ->
                    Color.parseColor("#C8E6C9") // verde claro
            }
        )



        holder.view.setOnClickListener { onClick(alerta) }
    }

    override fun getItemCount() = alertas.size
}