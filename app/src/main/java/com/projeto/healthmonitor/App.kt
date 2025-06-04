package com.projeto.healthmonitor

import android.app.Application
import com.projeto.healthmonitor.extensions.Notificacoes

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificacoes.criarCanais(this)
    }
}