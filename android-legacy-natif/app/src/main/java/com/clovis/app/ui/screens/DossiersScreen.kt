// Cree le 23/08/2026, Bourama : Lot 2 Partie 3 (app mobile), fichiers/dossiers designes.
//
// Deux niveaux : liste des dossiers designes (racine de l'ecran), puis
// navigation a l'interieur d'un dossier (pile de DossierDesigne -> sous-
// dossiers, style breadcrumb). Pas de bibliotheque de navigation ajoutee :
// meme approche state-driven que MainActivity.kt (Lot 1), une simple pile
// en memoire suffit pour ce niveau de profondeur.
package com.clovis.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clovis.app.data.DossierDesigne
import com.clovis.app.data.DossiersDesignesRepository
import com.clovis.app.data.ElementDossier

@Composable
fun DossiersScreen() {
    val context = LocalContext.current
    val repo = remember { DossiersDesignesRepository(context) }

    var dossiersDesignes by remember { mutableStateOf(repo.listerDossiersDesignes()) }
    // Pile de navigation : premier element = dossier designe ouvert, suivants = sous-dossiers.
    // Chaque entree : (uri du dossier courant, nom affiche, uri du PARENT direct pour un eventuel deplacement).
    var pile by remember { mutableStateOf<List<Triple<Uri, String, Uri?>>>(emptyList()) }
    var contenu by remember { mutableStateOf<List<ElementDossier>>(emptyList()) }

    var dialogueNouveauDossier by remember { mutableStateOf(false) }
    var dialogueNouveauFichier by remember { mutableStateOf(false) }
    var elementARenommer by remember { mutableStateOf<ElementDossier?>(null) }
    var elementASupprimer by remember { mutableStateOf<ElementDossier?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }

    fun rafraichirContenu() {
        val courant = pile.lastOrNull() ?: return
        contenu = repo.listerContenu(courant.first)
    }

    val selecteurDossier = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            repo.ajouterDossierDesigne(uri)
            dossiersDesignes = repo.listerDossiersDesignes()
        }
    }

    // --- Vue "a l'interieur d'un dossier" ---
    if (pile.isNotEmpty()) {
        val courant = pile.last()

        LaunchedEffect(courant.first) { rafraichirContenu() }

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { pile = pile.dropLast(1) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
                Text(
                    pile.joinToString(" / ") { it.second },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { dialogueNouveauDossier = true }) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Dossier")
                }
                OutlinedButton(onClick = { dialogueNouveauFichier = true }) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Fichier")
                }
            }
            Spacer(Modifier.height(12.dp))

            erreur?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            if (contenu.isEmpty()) {
                Text("Dossier vide.", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contenu) { element ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (element.estDossier) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                element.nom,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (!element.estDossier) {
                                Text(
                                    "${element.tailleOctets / 1024} Ko",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (element.estDossier) {
                            IconButton(onClick = {
                                pile = pile + Triple(element.uri, element.nom, courant.first)
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Ouvrir")
                            }
                        }
                        IconButton(onClick = { elementARenommer = element }) {
                            Icon(Icons.Default.Edit, contentDescription = "Renommer")
                        }
                        IconButton(onClick = { elementASupprimer = element }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (dialogueNouveauDossier) {
            DialogueTexte(
                titre = "Nouveau dossier",
                label = "Nom du dossier",
                onValider = { nom ->
                    erreur = if (repo.creerSousDossier(courant.first, nom)) null else "Impossible de créer ce dossier."
                    rafraichirContenu()
                    dialogueNouveauDossier = false
                },
                onAnnuler = { dialogueNouveauDossier = false }
            )
        }
        if (dialogueNouveauFichier) {
            DialogueTexte(
                titre = "Nouveau fichier",
                label = "Nom du fichier (avec extension)",
                onValider = { nom ->
                    erreur = if (repo.creerFichier(courant.first, nom)) null else "Impossible de créer ce fichier."
                    rafraichirContenu()
                    dialogueNouveauFichier = false
                },
                onAnnuler = { dialogueNouveauFichier = false }
            )
        }
        elementARenommer?.let { element ->
            DialogueTexte(
                titre = "Renommer",
                label = "Nouveau nom",
                valeurInitiale = element.nom,
                onValider = { nouveauNom ->
                    erreur = if (repo.renommer(element.uri, nouveauNom)) null else "Impossible de renommer."
                    rafraichirContenu()
                    elementARenommer = null
                },
                onAnnuler = { elementARenommer = null }
            )
        }
        elementASupprimer?.let { element ->
            AlertDialog(
                onDismissRequest = { elementASupprimer = null },
                title = { Text("Supprimer « ${element.nom} » ?") },
                text = { Text(if (element.estDossier) "Le dossier et tout son contenu seront supprimés." else "Le fichier sera supprimé définitivement.") },
                confirmButton = {
                    TextButton(onClick = {
                        erreur = if (repo.supprimer(element.uri)) null else "Impossible de supprimer."
                        rafraichirContenu()
                        elementASupprimer = null
                    }) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { elementASupprimer = null }) { Text("Annuler") }
                }
            )
        }
        return
    }

    // --- Vue racine : liste des dossiers designes ---
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Dossiers désignés", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Clovis ne peut lire ou modifier que les dossiers que tu choisis explicitement ici.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = { selecteurDossier.launch(null) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Désigner un dossier")
        }
        Spacer(Modifier.height(16.dp))

        if (dossiersDesignes.isEmpty()) {
            Text("Aucun dossier désigné pour l'instant.", style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn {
            items(dossiersDesignes) { dossier: DossierDesigne ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        dossier.nom,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        repo.retirerDossierDesigne(dossier.uri)
                        dossiersDesignes = repo.listerDossiersDesignes()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Retirer")
                    }
                    IconButton(onClick = {
                        pile = listOf(Triple(dossier.uri, dossier.nom, null))
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Ouvrir")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun DialogueTexte(
    titre: String,
    label: String,
    valeurInitiale: String = "",
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit
) {
    var valeur by remember { mutableStateOf(valeurInitiale) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(titre) },
        text = {
            OutlinedTextField(
                value = valeur,
                onValueChange = { valeur = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (valeur.isNotBlank()) onValider(valeur) }, enabled = valeur.isNotBlank()) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        }
    )
}
