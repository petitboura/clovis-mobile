// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Initialisation MANUELLE de Firebase (FirebaseOptions en dur), sans le
// plugin Gradle google-services : ce plugin exige un fichier
// google-services.json valide pour meme demarrer le build Gradle -- ca
// aurait casse la compilation du Lot 1 (deja livre) tant que Bourama n'a
// pas cree de projet Firebase. Meme philosophie que BASE_URL dans
// ClovisApiClient.kt : des constantes "A_REMPLACER_PAR_..." qui ne
// bloquent JAMAIS la compilation, seulement le fonctionnement reel tant
// qu'elles ne sont pas renseignees (firebaseConfigure() se contente de ne
// rien faire si elles sont encore a leur valeur par defaut).
//
// TODO Bourama : creer un projet Firebase (gratuit), activer Cloud
// Messaging. ATTENTION -- les deux flavors Android (play: com.clovis.app,
// externe: com.clovis.app.externe) ont des applicationId DIFFERENTS : il
// faut ajouter DEUX apps Android dans la console Firebase (meme projet),
// une par applicationId, sinon un seul flavor recevra des notifications.
// Les valeurs ci-dessous viennent de la page de config de chaque app
// (icone roue crantee > Parametres du projet > tes apps > SDK setup and
// configuration, section "Firebase SDK snippet" > Config).
package com.clovis.app.notifications

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

private const val FIREBASE_APPLICATION_ID = "A_REMPLACER_PAR_APPLICATION_ID_FIREBASE"
private const val FIREBASE_API_KEY = "A_REMPLACER_PAR_API_KEY_FIREBASE"
private const val FIREBASE_PROJECT_ID = "A_REMPLACER_PAR_PROJECT_ID_FIREBASE"
private const val FIREBASE_GCM_SENDER_ID = "A_REMPLACER_PAR_GCM_SENDER_ID_FIREBASE"

fun firebaseConfigureDisponible(): Boolean = !FIREBASE_APPLICATION_ID.startsWith("A_REMPLACER")

/**
 * Appelee une seule fois au demarrage (voir ClovisApplication.onCreate).
 * Ne fait rien tant que les constantes ci-dessus ne sont pas renseignees --
 * pas d'exception, juste pas de notifications natives pour l'instant.
 */
fun firebaseConfigure(context: Context) {
    if (!firebaseConfigureDisponible()) return
    if (FirebaseApp.getApps(context).isNotEmpty()) return

    val options = FirebaseOptions.Builder()
        .setApplicationId(FIREBASE_APPLICATION_ID)
        .setApiKey(FIREBASE_API_KEY)
        .setProjectId(FIREBASE_PROJECT_ID)
        .setGcmSenderId(FIREBASE_GCM_SENDER_ID)
        .build()
    FirebaseApp.initializeApp(context, options)
}
