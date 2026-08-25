// Cree le 23/08/2026, Bourama : Lot 4 Partie 3 (app mobile), controles de session.
//
// Portee (voir 04-controles-session.md) : DND et volume sonnerie/notifications
// pendant une session, restauration exacte de l'etat initial a la fin.
//
// Necessite la meme permission speciale "Acces a la Politique de Notification"
// pour les DEUX capacites : basculer le filtre d'interruption ET ajuster le
// volume des flux RING/NOTIFICATION (Android leve une SecurityException sur
// ces flux sans cette permission, contrairement au flux media). Meme famille
// que PACKAGE_USAGE_STATS au Lot 1 : pas de popup standard, redirection vers
// Reglages (ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).
//
// Decision prise ici, a valider avec Bourama si besoin d'ajuster : le DND de
// session utilise INTERRUPTION_FILTER_ALARMS (seules les alarmes passent) --
// le plus proche d'un vrai mode focus. Facile a changer vers PRIORITY si
// Bourama veut laisser passer certains contacts/apps prioritaires plus tard.
package com.clovis.app.data

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

data class EtatInitialSession(
    val filtreInterruptionInitial: Int,
    val volumeSonnerieInitial: Int,
    val volumeNotificationInitial: Int
)

class ControleSessionRepository(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun permissionAccordee(): Boolean = notificationManager.isNotificationPolicyAccessGranted

    fun ouvrirReglagesPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * A appeler au demarrage de la session, AVANT toute modification, pour
     * pouvoir restaurer exactement l'etat initial a l'arret -- voir critere
     * de fin du Lot 4.
     */
    fun capturerEtatInitial(): EtatInitialSession = EtatInitialSession(
        filtreInterruptionInitial = notificationManager.currentInterruptionFilter,
        volumeSonnerieInitial = audioManager.getStreamVolume(AudioManager.STREAM_RING),
        volumeNotificationInitial = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    )

    fun activerNePasDeranger() {
        if (!permissionAccordee()) return
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
    }

    fun couperSonnerieEtNotifications() {
        if (!permissionAccordee()) return
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
    }

    /**
     * Restaure l'etat exact capture par [capturerEtatInitial]. A appeler
     * systematiquement a l'arret de la session (bouton stop ET fin normale),
     * meme si une seule des deux bascules avait ete activee.
     */
    fun restaurerEtatInitial(etat: EtatInitialSession) {
        if (!permissionAccordee()) return
        notificationManager.setInterruptionFilter(etat.filtreInterruptionInitial)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, etat.volumeSonnerieInitial, 0)
        audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            etat.volumeNotificationInitial,
            0
        )
    }
}
