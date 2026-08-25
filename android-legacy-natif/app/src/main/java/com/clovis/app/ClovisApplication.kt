// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Premiere classe Application du projet (Lot 1 n'en avait pas besoin,
// tout demarrait directement dans MainActivity). Regroupe ici ce qui doit
// s'executer UNE SEULE FOIS au demarrage du process, avant meme la
// premiere Activity -- necessaire pour les canaux de notification (doivent
// exister avant tout appel a notify()) et Firebase (doit etre initialise
// avant toute utilisation du SDK, y compris par ClovisFirebaseMessagingService
// si le systeme le reveille sans passer par MainActivity).
package com.clovis.app

import android.app.Application
import com.clovis.app.notifications.creerCanauxNotifications
import com.clovis.app.notifications.firebaseConfigure

class ClovisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        firebaseConfigure(this)
        creerCanauxNotifications(this)
    }
}
