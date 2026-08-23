// Cree le 23/08/2026, Bourama : Lot 6, flavor externe uniquement.
// Modifie 23/08/2026, Lot 7 : ajout de deux sections quand le service est
// actif -- gestion des apps autorisees (une par une, voir AppsAutorisees.kt)
// et un panneau de test manuel pour executer/casser un scenario d'action
// (necessaire au critere de fin du Lot 7). Reste un seul ecran/onglet plutot
// que d'en ajouter un nouveau : Lot 7 prolonge directement le Lot 6, meme
// contexte (service d'accessibilite).
//
// Ecran de consentement affiche AVANT l'ecran d'activation natif d'Android
// (voir 06-service-accessibilite.md : "expliquer en langage simple ce que ce
// service permet concretement", avant l'avertissement systeme). Une fois
// actif, affiche le journal des observations (audit/debug, portee du Lot 6).
package com.clovis.app.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.accessibilite.AppsAutorisees
import com.clovis.app.accessibilite.EntreeAction
import com.clovis.app.accessibilite.EntreeJournal
import com.clovis.app.accessibilite.ExecuteurActions
import com.clovis.app.accessibilite.JournalAccessibilite
import com.clovis.app.accessibilite.JournalActions
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

    LaunchedEffect(Unit) { AppsAutorisees.initialiser(context) }

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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text("Service actif.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    SectionAppsAutorisees(context)
                    Spacer(Modifier.height(16.dp))
                    SectionTestAction()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Journal des dernières observations, pour vérifier que tout fonctionne :",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(entrees) { entree -> LigneObservation(entree) }
            }
        }
    }
}

/**
 * Portee du Lot 7 : autoriser une app "une par une", jamais d'action
 * generique. Liste des apps avec icone de lancement (queryIntentActivities
 * sur ACTION_MAIN/CATEGORY_LAUNCHER -- visibilite automatique sur Android
 * 11+, pas besoin de declarer <queries> pour ce cas precis).
 */
@Composable
private fun SectionAppsAutorisees(context: Context) {
    val autorisees by AppsAutorisees.autorisees.collectAsState()
    var appsInstallees by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        appsInstallees = listerAppsLancables(context)
    }

    Text("Apps autorisées pour les actions pilotées", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(
        "Clovis ne peut cliquer ou saisir du texte que dans les apps que tu autorises ici, une par une.",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp).heightIn(max = 260.dp)) {
            LazyColumn {
                items(appsInstallees) { (nomPaquet, libelle) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(libelle, modifier = Modifier.weight(1f))
                        Switch(
                            checked = autorisees.contains(nomPaquet),
                            onCheckedChange = { coche ->
                                if (coche) AppsAutorisees.autoriser(nomPaquet)
                                else AppsAutorisees.revoquer(nomPaquet)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Panneau de test manuel : necessaire pour verifier le critere de fin du
 * Lot 7 sur appareil reel (action de bout en bout + comportement de repli
 * quand on casse volontairement le scenario).
 */
@Composable
private fun SectionTestAction() {
    var texteCible by remember { mutableStateOf("") }
    var valeurSaisie by remember { mutableStateOf("") }
    var dernierResultat by remember { mutableStateOf<String?>(null) }
    val actions by JournalActions.entrees.collectAsState()

    Text("Tester une action (app autorisée au premier plan)", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = texteCible,
        onValueChange = { texteCible = it },
        label = { Text("Texte de l'élément ciblé") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = valeurSaisie,
        onValueChange = { valeurSaisie = it },
        label = { Text("Texte à saisir (pour le test de saisie)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Row {
        Button(onClick = {
            dernierResultat = ExecuteurActions.cliquerParTexte(texteCible).message
        }) { Text("Cliquer") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = {
            dernierResultat = ExecuteurActions.saisirTexteParCible(texteCible, valeurSaisie).message
        }) { Text("Saisir") }
    }
    dernierResultat?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(8.dp))
    Text("Dernières tentatives d'action :", style = MaterialTheme.typography.labelMedium)
    Column {
        actions.take(5).forEach { entree -> LigneAction(entree) }
    }
}

@Composable
private fun LigneObservation(entree: EntreeJournal) {
    val format = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text("${format.format(Date(entree.horodatage))} — ${entree.nomPaquet}")
        Text(
            "${entree.typeEvenement}, ${entree.nombreNoeudsLus} éléments lus",
            style = MaterialTheme.typography.bodySmall
        )
    }
    HorizontalDivider()
}

@Composable
private fun LigneAction(entree: EntreeAction) {
    val format = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val couleur = if (entree.succes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            "${format.format(Date(entree.horodatage))} — ${entree.nomPaquet} — ${if (entree.succes) "OK" else "ÉCHEC"}",
            color = couleur,
            style = MaterialTheme.typography.bodySmall
        )
        Text(entree.message, style = MaterialTheme.typography.bodySmall)
    }
}

/** Apps avec icone de lancement, hors Clovis lui-meme. */
private fun listerAppsLancables(context: Context): List<Pair<String, String>> {
    val intentLanceur = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val pm = context.packageManager
    return pm.queryIntentActivities(intentLanceur, PackageManager.MATCH_DEFAULT_ONLY)
        .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
        .distinctBy { it.first }
        .filter { it.first != context.packageName }
        .sortedBy { it.second.lowercase() }
}

private fun serviceActif(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val actifs = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return actifs.any {
        it.resolveInfo.serviceInfo.packageName == context.packageName &&
            it.resolveInfo.serviceInfo.name == ServiceAccessibiliteClovis::class.java.name
    }
}
