// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// `data` (pas `notification`) est utilise volontairement cote backend pour
// le champ "prioritaire" (voir clovis-backend/core/notifications_push.py) :
// un payload FCM de type "notification" est affiche automatiquement par le
// systeme SANS passer par onMessageReceived quand l'app est en arriere-
// plan, ce qui empecherait le repli plein-ecran/heads-up gere ici. Avec
// "data" uniquement, onMessageReceived est TOUJOURS appele, meme app fermee.
package com.clovis.app.notifications

import android.util.Log
import com.clovis.app.data.ClovisApiClient
import com.clovis.app.data.TokenPush
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClovisFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ClovisApiClient.enregistrerPushToken(TokenPush(plateforme = "android", token = token))
            } catch (e: Exception) {
                // Pas grave ici : retente au prochain lancement de MainActivity
                // (voir MainActivity.kt), le token FCM ne change pas souvent.
                Log.w("ClovisFCM", "Echec enregistrement token, retente au prochain lancement.", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val titre = message.data["title"] ?: "Clovis"
        val corps = message.data["body"] ?: ""
        val prioritaire = message.data["prioritaire"] == "true"
        NotificationsNatives.afficherRappel(applicationContext, titre, corps, prioritaire)
    }
}
