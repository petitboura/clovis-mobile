// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
// Modifie 23/08/2026, Lot 2 : ajout de la barre de navigation en bas
// (Usage / Dossiers), voir 00-commun.md et 02-fichiers-dossiers.md. Choix
// bottom nav (recommandation Material Design pour 3-5 destinations de
// niveau superieur) plutot qu'un simple bouton, en prevision des lots 3-5
// (notifications, controles de session, connecteurs) qui ajouteront chacun
// leur propre onglet.
// Etendu 23/08/2026, Lot 3 (notifications & rappels) : onglet Rappels +
// enregistrement du token FCM au demarrage (voir enregistrerTokenPushSiPossible).
package com.clovis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.clovis.app.accessibilite.ModuleAccessibilite
import com.clovis.app.data.ClovisApiClient
import com.clovis.app.data.SupabaseAuthClient
import com.clovis.app.data.TokenPush
import com.clovis.app.miseajour.ModuleMiseAJour
import com.clovis.app.notifications.firebaseConfigureDisponible
import com.clovis.app.ui.screens.ConnecteursScreen
import com.clovis.app.ui.screens.ControleSessionScreen
import com.clovis.app.ui.screens.DossiersScreen
import com.clovis.app.ui.screens.LoginScreen
import com.clovis.app.ui.screens.RappelsScreen
import com.clovis.app.ui.screens.UsageScreen
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Fusion Lot 2 (Usage/Dossiers) + Lot 4 (Session) + Lot 3 (Rappels) : on
// garde le pattern Scaffold+bottomBar du Lot 2 (deja pense pour accueillir
// les lots suivants), Session en premier onglet (ecran de demarrage
// naturel), Rappels ajoute a la suite de Connecteurs.
// Lot 6 : onglet ACCESSIBILITE ajoute a l'enum commune, mais affiche
// UNIQUEMENT si ModuleAccessibilite.disponible (vrai pour externe, faux pour
// play) -- voir plus bas. L'enum existe dans les deux flavors, seul l'ajout
// effectif a la bottomBar est conditionnel.
private enum class Onglet(val etiquette: String) {
    SESSION("Session"),
    USAGE("Usage"),
    DOSSIERS("Dossiers"),
    CONNECTEURS("Connecteurs"),
    RAPPELS("Rappels"),
    ACCESSIBILITE("Écran")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var connecte by remember { mutableStateOf(SupabaseAuthClient.estConnecte()) }
                    var ongletActif by remember { mutableStateOf(Onglet.SESSION) }
                    val scope = rememberCoroutineScope()

                    if (connecte) {
                        LaunchedEffect(Unit) {
                            scope.launch { enregistrerTokenPushSiPossible() }
                            scope.launch { rattraperActionsEnAttente() }
                        }
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
                                    NavigationBarItem(
                                        selected = ongletActif == Onglet.RAPPELS,
                                        onClick = { ongletActif = Onglet.RAPPELS },
                                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                        label = { Text(Onglet.RAPPELS.etiquette) }
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
                            Column(modifier = Modifier.fillMaxSize().padding(paddingInterne)) {
                                // Lot 8 : n'affiche rien reellement en flavor play
                                // (ModuleMiseAJour.disponible == false, voir stub).
                                if (ModuleMiseAJour.disponible) {
                                    ModuleMiseAJour.Banniere()
                                }
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    when (ongletActif) {
                                        Onglet.SESSION -> ControleSessionScreen()
                                        Onglet.USAGE -> UsageScreen()
                                        Onglet.DOSSIERS -> DossiersScreen()
                                        Onglet.CONNECTEURS -> ConnecteursScreen()
                                        Onglet.RAPPELS -> RappelsScreen()
                                        Onglet.ACCESSIBILITE -> ModuleAccessibilite.Ecran()
                                    }
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

    /**
     * Recupere le token FCM courant et l'envoie a clovis-backend. Ne fait
     * rien tant que Firebase n'est pas configure (voir
     * ClovisFirebaseApp.kt) -- pas d'erreur, juste pas de push natif pour
     * l'instant. Rappele a chaque connexion plutot qu'une seule fois au
     * premier lancement : couvre a la fois "premier lancement" et
     * "echec d'enregistrement precedent" (voir onNewToken, meme filet de
     * securite cote ClovisFirebaseMessagingService).
     */
    private suspend fun enregistrerTokenPushSiPossible() {
        if (!firebaseConfigureDisponible()) return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            ClovisApiClient.enregistrerPushToken(TokenPush(plateforme = "android", token = token))
        } catch (e: Exception) {
            // Pas grave : re-tente a la prochaine connexion.
        }
    }

    /**
     * Ajoute le 24/08/2026, Lot 1A (brancher le cerveau) : filet de secours
     * pour les actions decidees par Clovis pendant que l'app etait fermee
     * et dont le push n'est jamais arrive (app tuee, token pas encore
     * enregistre...). Meme esprit que enregistrerTokenPushSiPossible :
     * rappele a chaque connexion, best-effort, pas de retry ici.
     */
    private suspend fun rattraperActionsEnAttente() {
        try {
            val actions = ClovisApiClient.obtenirActionsEnAttente().actions
            for (action in actions) {
                com.clovis.app.data.ActionsAppareilExecuteur.executerAction(applicationContext, action.id)
            }
        } catch (e: Exception) {
            // Pas grave : re-tente a la prochaine connexion.
        }
    }
}
