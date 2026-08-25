// Cree le 23/08/2026, Bourama : Lot 5 Partie 3 (app mobile), connecteurs tiers.
//
// Un seul connecteur pour l'instant (Notion, priorite du critere de fin du
// Lot 5) -- structure prevue pour en accueillir d'autres plus tard (meme
// pattern demarrer/finaliser/statut cote clovis-backend, voir
// connexions/oauth_generique.py pour GitHub).
package com.clovis.app.ui.screens

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.data.ClovisApiClient
import com.clovis.app.data.ResultatNotion
import com.clovis.app.ui.RetourOAuth
import kotlinx.coroutines.launch

@Composable
fun ConnecteursScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notionConnecte by remember { mutableStateOf<Boolean?>(null) }
    var chargementConnexion by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var requeteRecherche by remember { mutableStateOf("") }
    var resultats by remember { mutableStateOf<List<ResultatNotion>>(emptyList()) }
    var recherchEnCours by remember { mutableStateOf(false) }

    suspend fun rafraichirStatut() {
        try {
            notionConnecte = ClovisApiClient.statutNotion().connecte
        } catch (e: Exception) {
            erreur = "Impossible de vérifier le statut Notion."
        }
    }

    LaunchedEffect(Unit) { rafraichirStatut() }

    // Ecoute le retour OAuth capte par OAuthCallbackActivity (Lot 5).
    LaunchedEffect(Unit) {
        RetourOAuth.evenements.collect { (code, state) ->
            chargementConnexion = true
            erreur = null
            try {
                ClovisApiClient.finaliserConnexionNotion(code, state)
                rafraichirStatut()
            } catch (e: Exception) {
                erreur = "Connexion Notion échouée."
            } finally {
                chargementConnexion = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Connecteurs tiers", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Clovis peut utiliser tes comptes sur d'autres apps, uniquement via leur connexion officielle.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        erreur?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notion", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    when (notionConnecte) {
                        true -> Icon(Icons.Default.CheckCircle, contentDescription = "Connecté", tint = MaterialTheme.colorScheme.primary)
                        false -> {}
                        null -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (notionConnecte == false) {
                    Button(
                        onClick = {
                            scope.launch {
                                chargementConnexion = true
                                erreur = null
                                try {
                                    val reponse = ClovisApiClient.demarrerConnexionNotion()
                                    val onglet = CustomTabsIntent.Builder().build()
                                    onglet.launchUrl(context, android.net.Uri.parse(reponse.url_autorisation))
                                } catch (e: Exception) {
                                    erreur = "Impossible de démarrer la connexion Notion."
                                } finally {
                                    chargementConnexion = false
                                }
                            }
                        },
                        enabled = !chargementConnexion,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (chargementConnexion) "Connexion en cours…" else "Connecter Notion")
                    }
                }

                if (notionConnecte == true) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = requeteRecherche,
                        onValueChange = { requeteRecherche = it },
                        label = { Text("Rechercher dans Notion") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                recherchEnCours = true
                                erreur = null
                                try {
                                    resultats = ClovisApiClient.rechercherNotion(requeteRecherche).resultats
                                } catch (e: Exception) {
                                    erreur = "Recherche Notion impossible."
                                } finally {
                                    recherchEnCours = false
                                }
                            }
                        },
                        enabled = !recherchEnCours,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (recherchEnCours) "Recherche…" else "Rechercher")
                    }

                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(resultats) { resultat ->
                            Text(
                                "${resultat.type} — ${resultat.id}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
