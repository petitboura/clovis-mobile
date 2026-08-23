// Cree le 23/08/2026, Bourama : Lot 6 (service d'accessibilite, flavor externe uniquement).
//
// PORTEE STRICTE DU LOT 6 : lecture seule. Aucun clic, aucun remplissage,
// aucune navigation pilotee -- ca, c'est le Lot 7 (hors scope ici, voir
// 06-service-accessibilite.md). Ce fichier ne doit jamais appeler de methode
// d'action sur un AccessibilityNodeInfo (performAction, etc.) tant que le
// Lot 7 n'est pas explicitement demarre.
//
// Config associee : res/xml/accessibility_service_config.xml (memes
// contraintes en miroir : pas de capabilite d'action declaree encore).
package com.clovis.app.accessibilite

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ServiceAccessibiliteClovis : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        journaliser(nomPaquet = "-", typeEvenement = "SERVICE_CONNECTE", nombreNoeudsLus = 0)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val nomPaquet = event.packageName?.toString() ?: return

        // Lecture complete de l'arbre uniquement au changement de fenetre
        // (ouverture d'une nouvelle app/ecran) : parcourir tout l'arbre sur
        // CHAQUE evenement de contenu serait couteux en performance (voir
        // le skill djiguigne-standards-dev, regle performance). Pour les
        // autres types d'evenements, on journalise juste l'evenement lui-meme.
        val nombreNoeuds = if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            rootInActiveWindow?.let { compterNoeuds(it) } ?: 0
        } else {
            0
        }

        journaliser(
            nomPaquet = nomPaquet,
            typeEvenement = AccessibilityEvent.eventTypeToString(event.eventType),
            nombreNoeudsLus = nombreNoeuds
        )
    }

    override fun onInterrupt() {
        journaliser(nomPaquet = "-", typeEvenement = "SERVICE_INTERROMPU", nombreNoeudsLus = 0)
    }

    /** Parcours en lecture seule, profondeur plafonnee pour eviter un arbre pathologique. */
    private fun compterNoeuds(noeud: AccessibilityNodeInfo, profondeur: Int = 0): Int {
        if (profondeur > 40) return 1
        var total = 1
        for (i in 0 until noeud.childCount) {
            noeud.getChild(i)?.let { enfant ->
                total += compterNoeuds(enfant, profondeur + 1)
                enfant.recycle()
            }
        }
        return total
    }

    private fun journaliser(nomPaquet: String, typeEvenement: String, nombreNoeudsLus: Int) {
        JournalAccessibilite.ajouter(
            EntreeJournal(
                nomPaquet = nomPaquet,
                typeEvenement = typeEvenement,
                nombreNoeudsLus = nombreNoeudsLus,
                horodatage = System.currentTimeMillis()
            )
        )
    }
}
