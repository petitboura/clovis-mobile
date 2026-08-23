// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Cible du setFullScreenIntent() (voir NotificationsNatives.kt). N'est
// affichee que si Android a effectivement autorise l'alerte plein ecran
// pour Clovis (voir CanalNotifications.kt pour le contexte -- pas garanti
// par defaut sur le flavor "play" depuis la politique Play 2025).
package com.clovis.app.notifications

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val EXTRA_TITRE_ALERTE = "titre"
const val EXTRA_CORPS_ALERTE = "corps"

class AlerteRappelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Affichage par-dessus l'ecran verrouille + reveil de l'ecran --
        // equivalent Android d'une alarme classique. setShowWhenLocked/
        // setTurnScreenOn existent depuis l'API 27, setPermittedUsage sur
        // KeyguardManager avant (deprecie mais garde en repli, minSdk=26).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)
            ?.requestDismissKeyguard(this, null)

        val titre = intent.getStringExtra(EXTRA_TITRE_ALERTE) ?: "Clovis"
        val corps = intent.getStringExtra(EXTRA_CORPS_ALERTE) ?: ""

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EcranAlerte(titre = titre, corps = corps, onFermer = { finish() })
                }
            }
        }
    }
}

@Composable
private fun EcranAlerte(titre: String, corps: String, onFermer: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titre, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(corps, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFermer) { Text("OK") }
    }
}
