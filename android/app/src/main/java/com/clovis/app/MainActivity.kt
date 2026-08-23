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
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.clovis.app.accessibilite.ModuleAccessibilite
import com.clovis.app.data.SupabaseAuthClient
import com.clovis.app.ui.screens.ConnecteursScreen
import com.clovis.app.ui.screens.ControleSessionScreen
import com.clovis.app.ui.screens.DossiersScreen
import com.clovis.app.ui.screens.LoginScreen
import com.clovis.app.ui.screens.UsageScreen

// Fusion Lot 2 (Usage/Dossiers) + Lot 4 (Session) : on garde le pattern
// Scaffold+bottomBar du Lot 2 (deja pense pour accueillir les lots suivants),
// Session ajoute en premier onglet -- c'est l'ecran de demarrage naturel de
// l'app au quotidien.
// Lot 6 : onglet ACCESSIBILITE ajoute a l'enum commune, mais affiche
// UNIQUEMENT si ModuleAccessibilite.disponible (vrai pour externe, faux pour
// play) -- voir plus bas. L'enum existe dans les deux flavors, seul l'ajout
// effectif a la bottomBar est conditionnel.
private enum class Onglet(val etiquette: String) {
    SESSION("Session"),
    USAGE("Usage"),
    DOSSIERS("Dossiers"),
    CONNECTEURS("Connecteurs"),
    ACCESSIBILITE("Écran")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var connecte by remember { mutableStateOf(SupabaseAuthClient.estConnecte()) }
                    if (connecte) {
                        var ongletActif by remember { mutableStateOf(Onglet.SESSION) }
                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = ongletActif == Onglet.SESSION,
                                        onClick = { ongletActif = Onglet.SESSION },
                                        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                        label = { Text(Onglet.SESSION.etiquette) }
                                    )
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
                                    // Lot 6 : n'existe reellement que dans le flavor externe
                                    // (ModuleAccessibilite.disponible == false et Ecran() vide
                                    // pour play, voir accessibilite/ModuleAccessibilite.kt).
                                    if (ModuleAccessibilite.disponible) {
                                        NavigationBarItem(
                                            selected = ongletActif == Onglet.ACCESSIBILITE,
                                            onClick = { ongletActif = Onglet.ACCESSIBILITE },
                                            icon = { Icon(Icons.Default.RemoveRedEye, contentDescription = null) },
                                            label = { Text(Onglet.ACCESSIBILITE.etiquette) }
                                        )
                                    }
                                }
                            }
                        ) { paddingInterne ->
                            Surface(modifier = Modifier.fillMaxSize().padding(paddingInterne)) {
                                when (ongletActif) {
                                    Onglet.SESSION -> ControleSessionScreen()
                                    Onglet.USAGE -> UsageScreen()
                                    Onglet.DOSSIERS -> DossiersScreen()
                                    Onglet.CONNECTEURS -> ConnecteursScreen()
                                    Onglet.ACCESSIBILITE -> ModuleAccessibilite.Ecran()
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
