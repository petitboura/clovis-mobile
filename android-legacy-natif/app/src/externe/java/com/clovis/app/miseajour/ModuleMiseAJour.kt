// Cree le 23/08/2026, Bourama : Lot 8, flavor externe uniquement.
package com.clovis.app.miseajour

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

object ModuleMiseAJour {
    const val disponible = true

    @Composable
    fun Banniere() {
        val context = LocalContext.current
        var info by remember { mutableStateOf<InfoMiseAJour?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            scope.launch {
                info = try {
                    VerificateurMiseAJour.verifier()
                } catch (e: Exception) {
                    null // Pas de connexion ou API indisponible : on n'embete pas l'etudiant.
                }
            }
        }

        info?.let { i ->
            Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version ${i.version} disponible")
                    TextButton(onClick = { VerificateurMiseAJour.ouvrirTelechargement(context, i) }) {
                        Text("Télécharger")
                    }
                }
            }
        }
    }
}
