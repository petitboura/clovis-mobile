// Lot 2A, 24/08/2026. ClovisTheme remplace les appels a MaterialTheme brut
// (Material Design par defaut, bleu/violet systeme) sur les ecrans natifs
// deja existants -- aucun nouvel ecran ajoute ici. Un seul theme sombre
// (pas de bascule clair/sombre demandee pour le mobile, contrairement au
// web qui en a deux).
package com.clovis.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ClovisColorScheme = darkColorScheme(
    primary = ClovisAccent1,
    onPrimary = Color0D0B(),
    secondary = ClovisAccent2,
    onSecondary = Color0D0B(),
    background = ClovisFond,
    onBackground = ClovisTexte,
    surface = ClovisSurface,
    onSurface = ClovisTexte,
    surfaceVariant = ClovisSurfaceHaute,
    onSurfaceVariant = ClovisTexteMuet,
    outline = ClovisBordureForte,
    outlineVariant = ClovisBordure,
    error = ClovisErreur,
    onError = ClovisTexte,
    inversePrimary = ClovisAccent2,
)

// Petite fonction utilitaire plutot qu'une nouvelle couleur nommee : le
// texte sur fond dore (boutons primaires) reprend le meme brun tres fonce
// que clovis-frontend (::selection { color: #1a0d02 } dans globals.css),
// pas du blanc/noir pur qui casserait le contraste chaud voulu.
private fun Color0D0B() = androidx.compose.ui.graphics.Color(0xFF1A0D02)

@Composable
fun ClovisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClovisColorScheme,
        typography = ClovisTypography,
        content = content,
    )
}
