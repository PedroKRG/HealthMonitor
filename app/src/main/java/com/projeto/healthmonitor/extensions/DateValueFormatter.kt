package com.projeto.healthmonitor.extensions

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.projeto.healthmonitor.model.RegistroDiario

class DateValueFormatter(private val registros: List<RegistroDiario>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = value.toInt()
        return if (index in registros.indices) registros[index].data else ""
    }
}