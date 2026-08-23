// Cree le 23/08/2026, Bourama : Lot 4 Partie 3 (app mobile), controles de session.
//
// Design : repris des references regardees avant de coder (Opal, One Sec,
// Forest) -- un seul geste central (demarrer/arreter), etat actif tres
// visible (minuteur + fond change de couleur), bouton stop toujours visible
// pendant la session plutot que cache dans un menu.
package com.clovis.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clovis.app.data.ControleSessionRepository
import com.clovis.app.data.EtatInitialSession
import kotlinx.coroutines.delay

@Composable
fun ControleSessionScreen() {
    val context = LocalContext.current
    val repo = remember { ControleSessionRepository(context) }

    var permissionOk by remember { mutableStateOf(repo.permissionAccordee()) }
    var sessionActive by remember { mutableStateOf(false) }
    var etatInitial by remember { mutableStateOf<EtatInitialSession?>(null) }
    var secoulees by remember { mutableStateOf(0) }

    var couperNotifications by remember { mutableStateOf(true) }
    var couperSonnerie by remember { mutableStateOf(true) }

    LaunchedEffect(sessionActive) {
        if (sessionActive) {
            secoulees = 0
            while (sessionActive) {
                delay(1000)
                secoulees += 1
            }
        }
    }

    fun demarrer() {
        etatInitial = repo.capturerEtatInitial()
        if (couperNotifications) repo.activerNePasDeranger()
        if (couperSonnerie) repo.couperSonnerieEtNotifications()
        sessionActive = true
    }

    fun arreter() {
        etatInitial?.let { repo.restaurerEtatInitial(it) }
        etatInitial = null
        sessionActive = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Session Clovis", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))

        if (!permissionOk) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Clovis a besoin d'un accès spécial pour couper les notifications et ajuster le volume pendant une session.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { repo.ouvrirReglagesPermission() }) {
                Text("Autoriser dans les réglages")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { permissionOk = repo.permissionAccordee() }) {
                Text("J'ai autorisé, actualiser")
            }
            return@Column
        }

        Spacer(Modifier.height(32.dp))

        if (!sessionActive) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Couper les notifications (Ne pas déranger)")
                        Switch(checked = couperNotifications, onCheckedChange = { couperNotifications = it })
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Couper la sonnerie et le son des notifications")
                        Switch(checked = couperSonnerie, onCheckedChange = { couperSonnerie = it })
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = sessionActive) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val minutes = secoulees / 60
                val secondes = secoulees % 60
                Text(
                    "%d:%02d".format(minutes, secondes),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("Session en cours", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
            }
        }

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (sessionActive) {
                Button(
                    onClick = { arreter() },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = CircleShape
                ) {
                    Text("Stop", fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = { demarrer() },
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape
                ) {
                    Text("Start", fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
