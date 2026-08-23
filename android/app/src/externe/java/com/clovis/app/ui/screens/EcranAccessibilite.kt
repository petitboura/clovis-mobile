// Cree le 23/08/2026, Bourama : Lot 6, flavor externe uniquement.
//
// Ecran de consentement affiche AVANT l'ecran d'activation natif d'Android
// (voir 06-service-accessibilite.md : "expliquer en langage simple ce que ce
// service permet concretement", avant l'avertissement systeme). Une fois
// actif, affiche le journal des observations (audit/debug, portee du Lot 6).
package com.clovis.app.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.accessibilite.EntreeJournal
import com.clovis.app.accessibilite.JournalAccessibilite
import com.clovis.app.accessibilite.ServiceAccessibiliteClovis
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EcranAccessibilite() {
    val context = LocalContext.current
    var actif by remember { mutableStateOf(serviceActif(context)) }
    val entrees by JournalAccessibilite.entrees.collectAsState()

    // Reverifie l'etat regulierement : l'etudiant peut activer/desactiver le
    // service depuis les reglages systeme pendant que cet ecran est ouvert
    // en arriere-plan (pas de callback direct possible depuis l'ecran).
    LaunchedEffect(Unit) {
        while (true) {
            actif = serviceActif(context)
            delay(1500)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Lecture d'écran", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (!actif) {
            Text(
                "Ce service permet à Clovis de lire ce qui s'affiche à l'écran dans tes " +
                    "autres applications, pour pouvoir plus tard t'aider directement dedans. " +
                    "Pour l'instant, Clovis lit seulement : il n'appuie sur rien et ne modifie rien.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Concrètement : quand tu ouvres une app, Clovis peut voir les éléments " +
                    "affichés (textes, boutons), un peu comme un lecteur d'écran pour " +
                    "malvoyants. Tu peux désactiver ça à tout moment dans les réglages du téléphone.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }) {
                Text("Continuer vers les réglages")
            }
        } else {
            Text("Service actif.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Journal des dernières observations, pour vérifier que tout fonctionne :",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(8.dp))
            JournalObservations(entrees)
        }
    }
}

@Composable
private fun JournalObservations(entrees: List<EntreeJournal>) {
    val format = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(entrees) { entree ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Text("${format.format(Date(entree.horodatage))} — ${entree.nomPaquet}")
                Text(
                    "${entree.typeEvenement}, ${entree.nombreNoeudsLus} éléments lus",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider()
        }
    }
}

private fun serviceActif(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val actifs = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return actifs.any {
        it.resolveInfo.serviceInfo.packageName == context.packageName &&
            it.resolveInfo.serviceInfo.name == ServiceAccessibiliteClovis::class.java.name
    }
}
