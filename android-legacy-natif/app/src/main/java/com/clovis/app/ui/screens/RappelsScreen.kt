// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Ecran de validation du Lot 3 (meme role que UsageScreen.kt pour le
// Lot 1) : chaque capacite native est declenchable ici manuellement, pour
// verifier sur appareil reel qu'elle fonctionne avant de considerer le lot
// termine (voir criteres de fin, 03-notifications-rappels.md). Pas
// l'interface finale que l'etudiant verra au quotidien (les rappels
// arriveront normalement via push depuis une conversation avec Clovis,
// pas depuis cet ecran) -- un futur ecran "Mes rappels" plus soigne reste
// a faire si Bourama le demande.
package com.clovis.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.notifications.NotificationsNatives
import com.clovis.app.notifications.RappelsNatifs
import java.util.Calendar

@Composable
fun RappelsScreen() {
    val context = LocalContext.current

    var permissionNotifOk by remember {
        mutableStateOf(NotificationsNatives.permissionNotificationsAccordee(context))
    }
    val lanceurPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee -> permissionNotifOk = accordee }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Notifications & rappels", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Écran de test du Lot 3 -- les rappels réels arriveront via Clovis, ceci sert à vérifier que chaque capacité fonctionne sur cet appareil.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(20.dp))

        if (!permissionNotifOk) {
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Clovis n'a pas encore le droit d'envoyer des notifications.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            lanceurPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }) {
                        Text("Autoriser les notifications")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Notification classique", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                NotificationsNatives.afficherRappel(
                    context, "Rappel Clovis", "Ceci est une notification de test.", prioritaire = false
                )
            },
            enabled = permissionNotifOk,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Envoyer une notification de test") }

        Spacer(Modifier.height(20.dp))

        Text("Alerte prioritaire (plein écran)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        val pleinEcranAutorise = NotificationsNatives.pleinEcranAutorise(context)
        if (!pleinEcranAutorise) {
            Text(
                "Non autorisé par Android pour l'instant (voir Réglages).",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = { NotificationsNatives.ouvrirReglagesPleinEcran(context) }) {
                Text("Ouvrir les réglages")
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                NotificationsNatives.afficherRappel(
                    context, "Rappel important", "Ceci est une alerte prioritaire de test.", prioritaire = true
                )
            },
            enabled = permissionNotifOk,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Envoyer une alerte prioritaire de test") }

        Spacer(Modifier.height(20.dp))

        Text("Alarme", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val maintenant = Calendar.getInstance()
                RappelsNatifs.creerAlarme(
                    context, "Rappel Clovis",
                    maintenant.get(Calendar.HOUR_OF_DAY), maintenant.get(Calendar.MINUTE)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Créer une alarme de test (maintenant)") }

        Spacer(Modifier.height(20.dp))

        Text("Calendrier", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val debut = Calendar.getInstance().timeInMillis
                val fin = debut + 60 * 60 * 1000
                RappelsNatifs.ajouterEvenementCalendrier(
                    context, "Événement Clovis (test)", "Ajouté depuis l'écran de test du Lot 3.", debut, fin
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ajouter un événement de test") }
    }
}
