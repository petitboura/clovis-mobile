// Cree le 23/08/2026, Bourama : Lot 5 Partie 3 (app mobile), connecteurs tiers.
//
// Activite invisible (theme translucide) dediee a intercepter la
// redirection OAuth (clovismobile://oauth-callback?code=...&state=...),
// declenchee par Android via le intent-filter du manifeste apres que
// Custom Tabs a suivi la redirection depuis Notion. Ne montre aucune UI :
// extrait code/state, les publie via RetourOAuth (collecte par
// ConnecteursScreen), puis relance MainActivity et se termine.
package com.clovis.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.clovis.app.MainActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.DelicateCoroutinesApi

/** Code + state recus lors d'un retour OAuth, publies pour ConnecteursScreen. */
object RetourOAuth {
    val evenements = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
}

class OAuthCallbackActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        val state = uri?.getQueryParameter("state")

        if (code != null && state != null) {
            // GlobalScope volontaire ici : cette Activity se termine tout de
            // suite (finish() juste apres), une portee liee a son cycle de
            // vie serait annulee avant que l'evenement parte.
            GlobalScope.launch { RetourOAuth.evenements.emit(code to state) }
        }

        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }
}
