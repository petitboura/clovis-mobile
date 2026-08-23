// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
//
// Lecture du temps passe par app aujourd'hui, via l'API systeme Android
// UsageStatsManager (voir 01-socle-app-android.md). Necessite une permission
// speciale accordee manuellement par l'etudiant dans Reglages (pas une popup
// standard) -- voir permissionAccordee() / ouvrirReglagesPermission().
package com.clovis.app.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.util.Calendar

data class UsageApp(val nomPaquet: String, val dureeSecondes: Long)

class UsageStatsRepository(private val context: Context) {

    fun permissionAccordee(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** A appeler depuis un bouton "Autoriser" -- pas de popup standard possible ici. */
    fun ouvrirReglagesPermission() {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    /** Usage du jour courant (minuit a maintenant), par app, trie decroissant. */
    fun usageAujourdhui(): List<UsageApp> {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val debutJour = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val maintenant = System.currentTimeMillis()

        val stats = manager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, debutJour, maintenant
        )

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .map { UsageApp(it.packageName, it.totalTimeInForeground / 1000) }
            .sortedByDescending { it.dureeSecondes }
    }

    /** Paquet de l'app actuellement au premier plan, ou null si indetectable. */
    fun appActuellementActive(): String? {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val maintenant = System.currentTimeMillis()
        val events = manager.queryEvents(maintenant - 60_000, maintenant)
        var dernierPaquetActif: String? = null
        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                dernierPaquetActif = event.packageName
            }
        }
        return dernierPaquetActif
    }
}
