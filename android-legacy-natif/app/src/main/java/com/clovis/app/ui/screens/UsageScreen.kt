// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
//
// Ecran minimal du Lot 1 : temps passe par app + app actuellement active,
// pour valider que UsageStatsManager -> ecran -> backend fonctionne
// reellement (critere de fin du lot, voir 01-socle-app-android.md).
//
// Design : repris des references regardees avant de coder (Digital
// Wellbeing Android, Screen Time iOS) -- total du jour en haut, app active
// mise en avant, puis liste des apps triee par duree decroissante.
package com.clovis.app.ui.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.data.ClovisApiClient
import com.clovis.app.data.EntreeUsage
import com.clovis.app.data.SynchronisationUsage
import com.clovis.app.data.UsageApp
import com.clovis.app.data.UsageStatsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun UsageScreen() {
    val context = LocalContext.current
    val repo = remember { UsageStatsRepository(context) }
    val scope = rememberCoroutineScope()

    var permissionOk by remember { mutableStateOf(repo.permissionAccordee()) }
    var apps by remember { mutableStateOf<List<UsageApp>>(emptyList()) }
    var appActive by remember { mutableStateOf<String?>(null) }
    var messageSync by remember { mutableStateOf<String?>(null) }

    fun rafraichir() {
        if (!repo.permissionAccordee()) {
            permissionOk = false
            return
        }
        permissionOk = true
        apps = repo.usageAujourdhui()
        appActive = repo.appActuellementActive()
    }

    LaunchedEffect(Unit) { rafraichir() }

    fun nomLisible(paquet: String): String = try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(paquet, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        paquet
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Temps d'écran aujourd'hui", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))

        if (!permissionOk) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Clovis a besoin d'accéder aux statistiques d'usage pour afficher ton temps par app.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { repo.ouvrirReglagesPermission() }) {
                Text("Autoriser dans les réglages")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { rafraichir() }) {
                Text("J'ai autorisé, actualiser")
            }
            return@Column
        }

        val totalSecondes = apps.sumOf { it.dureeSecondes }
        Text(
            "${totalSecondes / 3600}h ${(totalSecondes % 3600) / 60}min au total",
            style = MaterialTheme.typography.bodyLarge
        )

        appActive?.let {
            Spacer(Modifier.height(12.dp))
            Card {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active maintenant : ", style = MaterialTheme.typography.labelLarge)
                    Text(nomLisible(it), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(apps) { app ->
                val maxDuree = (apps.maxOfOrNull { it.dureeSecondes } ?: 1L).coerceAtLeast(1L)
                val proportion = (app.dureeSecondes.toFloat() / maxDuree).coerceIn(0.02f, 1f)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nomLisible(app.nomPaquet))
                        Text("${app.dureeSecondes / 60} min")
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { proportion },
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                }
            }
        }

        messageSync?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        val entrees = apps.map {
                            EntreeUsage(
                                nom_app = it.nomPaquet,
                                date = LocalDate.now().toString(),
                                duree_secondes = it.dureeSecondes.toInt()
                            )
                        }
                        ClovisApiClient.synchroniserUsage(
                            SynchronisationUsage(plateforme = "android", entrees = entrees)
                        )
                        messageSync = "Synchronisé avec Clovis."
                    } catch (e: Exception) {
                        messageSync = "Échec de synchronisation, réessaie."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Synchroniser avec Clovis")
        }
    }
}
