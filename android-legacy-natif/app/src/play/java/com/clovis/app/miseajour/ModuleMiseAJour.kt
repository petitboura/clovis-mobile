// Cree le 23/08/2026, Bourama : flavor play.
//
// Le flavor "play" se met a jour automatiquement via le Play Store, ce
// module n'a pas de raison d'exister ici. Stub vide pour garder
// MainActivity.kt (src/main, commun) compilable sans code specifique a
// "externe" -- meme principe que ModuleAccessibilite (voir Lot 6).
package com.clovis.app.miseajour

import androidx.compose.runtime.Composable

object ModuleMiseAJour {
    const val disponible = false

    @Composable
    fun Banniere() {
        // Jamais appele : disponible = false.
    }
}
