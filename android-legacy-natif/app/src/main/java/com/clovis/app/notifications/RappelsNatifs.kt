// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Trois capacites via intents standards (pas de ContentResolver direct,
// donc pas de permission dangereuse READ/WRITE_CALENDAR a demander --
// l'app native concernee affiche sa propre UI de confirmation, ce qui
// est aussi plus sur pour l'etudiant qu'un ajout silencieux) :
// - Alarme  : AlarmClock.ACTION_SET_ALARM vers l'app Horloge.
// - Calendrier : Intent.ACTION_INSERT vers CalendarContract.Events
//   (c'est toujours le "Calendar Provider" demande par le document de
//   cadrage -- juste delegue a l'app Calendrier plutot qu'insere en
//   silence, cf. ci-dessus).
// - Ouverture d'app : PackageManager.getLaunchIntentForPackage.
//
// TROUVE EN CONSTRUISANT CE LOT, A REMONTER A BOURAMA (absent du document
// de cadrage) : depuis Android 11, resolveActivity()/queryIntentActivities()
// sur un package qui n'est pas explicitement declare dans un bloc
// <queries> de AndroidManifest.xml renvoie systematiquement "rien trouve",
// meme si l'app cible est bien installee (restriction de visibilite des
// packages). Consequence concrete : "ouvrir une app a la demande" pour une
// app tierce QUELCONQUE (ex: celle que l'etudiant nomme dans une
// conversation avec Clovis) n'est PAS libre sur Android non plus, contrairement
// a ce que 03-notifications-rappels.md laisse entendre ("Android : intent
// de lancement" presente comme sans restriction face a la limite iOS).
// Chaque app tierce a ouvrir devrait etre listee a l'avance dans <queries>
// (voir AndroidManifest.xml, section actuellement vide -- WhatsApp/
// Calculatrice/etc. a ajouter un par un si Bourama confirme lesquelles),
// ou bien demander la permission QUERY_ALL_PACKAGES (fortement deconseillee
// par Google Play, justificatif exige a la soumission). Pour l'instant,
// ouvrirApp() ci-dessous fonctionne uniquement pour un package explicitement
// ajoute a <queries>.
package com.clovis.app.notifications

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast

object RappelsNatifs {

    fun creerAlarme(context: Context, message: String, heure: Int, minute: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, heure)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false) // l'etudiant confirme dans l'app Horloge, jamais silencieux
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Aucune application Horloge trouvée sur cet appareil.", Toast.LENGTH_LONG).show()
        }
    }

    fun ajouterEvenementCalendrier(
        context: Context,
        titre: String,
        description: String,
        debutMillis: Long,
        finMillis: Long
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titre)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, debutMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Aucune application Calendrier trouvée sur cet appareil.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Ne fonctionne que pour un `nomPaquet` deja declare dans le bloc
     * <queries> de AndroidManifest.xml (voir note en tete de fichier).
     */
    fun ouvrirApp(context: Context, nomPaquet: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(nomPaquet) ?: return false
        context.startActivity(intent)
        return true
    }
}
