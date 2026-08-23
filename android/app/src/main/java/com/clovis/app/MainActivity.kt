// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
// Modifie 23/08/2026, Lot 2 : ajout de la barre de navigation en bas
// (Usage / Dossiers), voir 00-commun.md et 02-fichiers-dossiers.md. Choix
// bottom nav (recommandation Material Design pour 3-5 destinations de
// niveau superieur) plutot qu'un simple bouton, en prevision des lots 3-5
// (notifications, controles de session, connecteurs) qui ajouteront chacun
// leur propre onglet.
package com.clovis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.clovis.app.data.SupabaseAuthClient
import com.clovis.app.ui.screens.ConnecteursScreen
import com.clovis.app.ui.screens.DossiersScreen
import com.clovis.app.ui.screens.LoginScreen
import com.clovis.app.ui.screens.UsageScreen

private enum class Onglet(val etiquette: String) {
    USAGE("Usage"),
    DOSSIERS("Dossiers"),
    CONNECTEURS("Connecteurs")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var connecte by remember { mutableStateOf(SupabaseAuthClient.estConnecte()) }
                    if (connecte) {
                        var ongletActif by remember { mutableStateOf(Onglet.USAGE) }
                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = ongletActif == Onglet.USAGE,
                                        onClick = { ongletActif = Onglet.USAGE },
                                        icon = { Icon(Icons.Default.QueryStats, contentDescription = null) },
                                        label = { Text(Onglet.USAGE.etiquette) }
                                    )
                                    NavigationBarItem(
                                        selected = ongletActif == Onglet.DOSSIERS,
                                        onClick = { ongletActif = Onglet.DOSSIERS },
                                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                        label = { Text(Onglet.DOSSIERS.etiquette) }
                                    )
                                    NavigationBarItem(
                                        selected = ongletActif == Onglet.CONNECTEURS,
                                        onClick = { ongletActif = Onglet.CONNECTEURS },
                                        icon = { Icon(Icons.Default.Link, contentDescription = null) },
                                        label = { Text(Onglet.CONNECTEURS.etiquette) }
                                    )
                                }
                            }
                        ) { paddingInterne ->
                            Surface(modifier = Modifier.fillMaxSize().padding(paddingInterne)) {
                                when (ongletActif) {
                                    Onglet.USAGE -> UsageScreen()
                                    Onglet.DOSSIERS -> DossiersScreen()
                                    Onglet.CONNECTEURS -> ConnecteursScreen()
                                }
                            }
                        }
                    } else {
                        LoginScreen(onConnecte = { connecte = true })
                    }
                }
            }
        }
    }
}
