// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Deux canaux distincts (voir 03-notifications-rappels.md) :
// - CANAL_RAPPELS : notification classique (banniere standard).
// - CANAL_RAPPELS_URGENTS : categorie ALARM, eligible a l'alerte plein
//   ecran par-dessus le verrouillage -- MAIS voir la note critique
//   ci-dessous, cette eligibilite ne suffit plus a elle seule depuis 2025.
//
// IMPORTANT (a documenter aupres de Bourama, trouve en recherche avant de
// coder ce lot -- pas dans le document de cadrage) : depuis une politique
// Google Play appliquee courant 2025, la permission USE_FULL_SCREEN_INTENT
// n'est accordee PAR DEFAUT qu'aux apps dont Play Store classe la fonction
// principale comme appels ou alarmes. Clovis n'est ni l'un ni l'autre :
// sur le flavor "play", cette permission sera tres probablement refusee/
// revoquee silencieusement -- Android retombe alors sur une notification
// heads-up classique (pas d'ecran plein), sauf si l'etudiant l'active
// lui-meme dans Reglages > Applications > Clovis > Notifications plein
// ecran. Voir NotificationsNatives.afficherRappel() pour le comportement
// de repli, et RappelsScreen.kt pour le bouton qui renvoie vers ce reglage.
package com.clovis.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val CANAL_RAPPELS = "rappels"
const val CANAL_RAPPELS_URGENTS = "rappels_urgents"

fun creerCanauxNotifications(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val gestionnaire = context.getSystemService(NotificationManager::class.java)

    val canalStandard = NotificationChannel(
        CANAL_RAPPELS,
        "Rappels Clovis",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Rappels et notifications programmés par Clovis."
    }

    val canalUrgent = NotificationChannel(
        CANAL_RAPPELS_URGENTS,
        "Rappels prioritaires Clovis",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Rappels importants (type alarme) -- affichage plein écran si autorisé."
    }

    gestionnaire.createNotificationChannel(canalStandard)
    gestionnaire.createNotificationChannel(canalUrgent)
}
