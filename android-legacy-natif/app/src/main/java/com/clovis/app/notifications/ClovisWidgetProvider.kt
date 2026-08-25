// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Widget minimal (Option A du raccourci "depuis partout", voir
// ClovisTileService.kt pour le contexte complet) : une icone/etiquette sur
// l'ecran d'accueil qui ouvre l'app. AppWidgetProvider classique plutot
// que Glance (Jetpack Compose pour widgets) pour ne pas ajouter de
// nouvelle dependance juste pour un widget aussi simple -- a reconsiderer
// si un widget plus riche est demande plus tard (ex: prochain rappel
// affiche directement dessus).
package com.clovis.app.notifications

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.clovis.app.MainActivity
import com.clovis.app.R

class ClovisWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val vues = RemoteViews(context.packageName, R.layout.widget_clovis)
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            vues.setOnClickPendingIntent(R.id.widget_clovis_racine, pending)
            appWidgetManager.updateAppWidget(id, vues)
        }
    }
}
