// Cree le 24/08/2026, Bourama : Lot 1A Partie 3 (app mobile), brancher le
// cerveau -- point d'arrivee cote telephone d'une action decidee par
// Clovis (voir clovis-backend/core/actions_appareil_mobile.py).
//
// VOLONTAIREMENT AUCUN type_action n'est encore reconnu ici : deux
// candidats identifies pendant ce lot (fichiers/dossiers, session
// DND/volume) ont chacun un point non tranche avec Bourama (voir le
// docstring de actions_appareil_mobile.py cote backend) avant de les
// brancher pour de vrai. Ce fichier est le point d'extension UNIQUE :
// ajouter un type_action = ajouter une branche dans le `when` ci-dessous,
// rien d'autre a toucher (le canal reseau/push est deja complet et
// fonctionnel de bout en bout).
//
// Chaque branche recoit `action.parametres` (JsonObject brut, forme
// dependant du type_action) et doit renvoyer un ResultatAction --
// executerAction() se charge ensuite de le rapporter au backend, y
// compris pour un type_action inconnu (echec explicite plutot que
// silence, voir la regle "anticiper les erreurs" des standards de dev).
package com.clovis.app.data

import android.content.Context
import android.util.Log

object ActionsAppareilExecuteur {

    private const val TAG = "ActionsAppareil"

    /**
     * Appelee a la reception du push type="action" (voir
     * ClovisFirebaseMessagingService.onMessageReceived) ET au filet de
     * secours (obtenirActionsEnAttente, a appeler au lancement de
     * MainActivity -- meme logique que le renouvellement du token push).
     */
    suspend fun executerAction(context: Context, actionId: String) {
        val action = try {
            ClovisApiClient.obtenirAction(actionId)
        } catch (e: Exception) {
            Log.w(TAG, "Echec recuperation action $actionId, abandon (pas de retry ici).", e)
            return
        }

        val resultat = when (action.type_action) {
            // Aucune branche reelle encore -- voir note en tete de fichier.
            else -> ResultatAction(
                succes = false,
                resultat = "type_action \"${action.type_action}\" non reconnu par l'app."
            )
        }

        try {
            ClovisApiClient.rapporterResultatAction(actionId, resultat)
        } catch (e: Exception) {
            Log.w(TAG, "Echec rapport resultat pour action $actionId (pas de retry ici).", e)
        }
    }
}
