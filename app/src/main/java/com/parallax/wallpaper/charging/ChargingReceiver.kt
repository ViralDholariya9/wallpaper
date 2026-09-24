package com.parallax.wallpaper.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ChargingReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHARGER_DISCONNECTED_LOCAL = "com.parallax.wallpaper.CHARGER_DISCONNECTED_LOCAL"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("ChargingReceiver", "Received power broadcast: $action")

        ChargingManager.init(context)

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (ChargingManager.isChargingEnabled.value) {
                    val activityIntent = Intent(context, ChargingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    try {
                        context.startActivity(activityIntent)
                        Log.d("ChargingReceiver", "Successfully launched ChargingActivity on charger connect")
                    } catch (e: Exception) {
                        Log.e("ChargingReceiver", "Failed to launch ChargingActivity: ${e.message}")
                    }
                } else {
                    Log.d("ChargingReceiver", "Charging animation is disabled by user in settings")
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                // Notify any active ChargingActivity to dismiss
                val dismissIntent = Intent(ACTION_CHARGER_DISCONNECTED_LOCAL).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(dismissIntent)
            }
        }
    }
}
