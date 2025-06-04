package com.projeto.healthmonitor.extensions

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.projeto.healthmonitor.R

object Notificacoes {

    fun criarCanais(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val canalPaciente = NotificationChannel(
                "canal_alertas",
                "Canal de Alertas para Paciente",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal usado para alertas de saúde para o paciente"
            }

            val canalMedico = NotificationChannel(
                "canal_alertass",
                "Canal de Alertas para Médico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal usado para alertas de saúde para o médico"
            }

            notificationManager.createNotificationChannel(canalPaciente)
            notificationManager.createNotificationChannel(canalMedico)
        }
    }

    fun enviarNotificacao(
        context: Context,
        titulo: String,
        mensagem: String,
        activityDestino: Class<*>,
        pacienteId: Long,
        destinatario: String
    ) {
        val sharedPref = context.getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("usuario_id", -1L)
        val tipoUsuario = sharedPref.getString("tipo_usuario", "") ?: ""


        if ((destinatario == "paciente" && tipoUsuario != "paciente") ||
            (destinatario == "medico" && tipoUsuario != "medico")
        ) {
            return
        }

        if (destinatario == "paciente" && usuarioId != pacienteId) {
            return
        }

        val canalId = when (destinatario) {
            "paciente" -> "canal_alertas"
            "medico" -> "canal_alertass"
            else -> return
        }

        val idNotificacao = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val intent = Intent(context, activityDestino).apply {
            putExtra("mensagem", mensagem)
            putExtra("pacienteId", pacienteId)
            putExtra("destinatario", destinatario)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            idNotificacao,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(idNotificacao, builder.build())
    }
}