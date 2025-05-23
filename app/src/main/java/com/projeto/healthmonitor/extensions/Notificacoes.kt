package com.projeto.healthmonitor.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.projeto.healthmonitor.R

object Notificacoes {

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_alertas",
                "Alertas de Saúde",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de alertas de pressão e glicemia"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    fun enviarNotificacao(
        context: Context,
        titulo: String,
        mensagem: String,
        id: Int,
        activityDestino: Class<*>,
        pacienteId: Long? = null // <- parâmetro opcional
    ) {
        val intent = Intent(context, activityDestino).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (pacienteId != null) {
                putExtra("pacienteId", pacienteId)
            }
            putExtra("mensagem", mensagem)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(context, "canal_alertas")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notificacao)
    }
}