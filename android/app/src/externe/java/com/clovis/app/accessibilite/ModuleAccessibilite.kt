// Cree le 23/08/2026, Bourama : Lot 6, flavor externe uniquement.
//
// Point de branchement entre MainActivity.kt (src/main, commun aux deux
// flavors) et l'ecran d'accessibilite (specifique a externe). MainActivity
// reference "ModuleAccessibilite" par son nom qualifie complet ; Gradle
// resout vers CE fichier pour le flavor externe, et vers
// src/play/.../ModuleAccessibilite.kt (stub vide) pour le flavor play.
// Voir 00-commun.md : isolation au niveau des sources compilees.
package com.clovis.app.accessibilite

import androidx.compose.runtime.Composable
import com.clovis.app.ui.screens.EcranAccessibilite

object ModuleAccessibilite {
    const val disponible = true

    @Composable
    fun Ecran() {
        EcranAccessibilite()
    }
}
