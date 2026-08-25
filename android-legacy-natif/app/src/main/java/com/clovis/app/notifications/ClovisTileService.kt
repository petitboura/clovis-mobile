// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Option A du raccourci "Clovis joignable depuis partout" (voir echange
// avec Bourama, 23/08/2026) : tuile dans le panneau reglages rapides
// (celui qu'on glisse depuis le haut de l'ecran), disponible sur les DEUX
// flavors et jamais restreinte -- a la difference de la bulle flottante
// par-dessus une autre app (Option B), qui reste hors scope ici et
// appartient aux lots 6-8 (accessibilite, flavor externe uniquement).
//
// L'etudiant doit ajouter la tuile lui-meme la premiere fois (glisser
// vers le bas > icone crayon > glisser la tuile Clovis dans la zone
// active) -- aucune API ne permet de l'ajouter automatiquement en dessous
// de l'API 33 (TileService.requestAddTileService existe depuis l'API 33
// seulement, pas utilise ici pour rester compatible minSdk=26 sans
// dupliquer un chemin de code juste pour ca).
package com.clovis.app.notifications

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.clovis.app.MainActivity

class ClovisTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivityAndCollapse(
                android.app.PendingIntent.getActivity(
                    this, 0, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
