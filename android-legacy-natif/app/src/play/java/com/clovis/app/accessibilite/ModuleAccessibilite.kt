// Cree le 23/08/2026, Bourama : flavor play.
//
// Le service d'accessibilite (lots 6-8) n'existe PAS dans cette variante,
// volontairement -- voir 00-commun.md. Ce stub garde MainActivity.kt
// (src/main, commun) compilable pour "play" sans jamais inclure la moindre
// ligne de code d'accessibilite dans l'APK Play Store : aucune classe
// AccessibilityService, aucun accessibility_service_config.xml, rien.
package com.clovis.app.accessibilite

import androidx.compose.runtime.Composable

object ModuleAccessibilite {
    const val disponible = false

    @Composable
    fun Ecran() {
        // Jamais appele : disponible = false, MainActivity ne route jamais ici pour ce flavor.
    }
}
