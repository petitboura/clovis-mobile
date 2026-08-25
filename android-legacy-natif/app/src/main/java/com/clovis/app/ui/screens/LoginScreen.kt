// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
package com.clovis.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.clovis.app.data.SupabaseAuthClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onConnecte: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var enCours by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Clovis", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = motDePasse,
            onValueChange = { motDePasse = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        erreur?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                erreur = null
                enCours = true
                scope.launch {
                    try {
                        SupabaseAuthClient.connexion(email, motDePasse)
                        onConnecte()
                    } catch (e: Exception) {
                        erreur = "Connexion impossible, vérifie tes identifiants."
                    } finally {
                        enCours = false
                    }
                }
            },
            enabled = !enCours && email.isNotBlank() && motDePasse.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enCours) "Connexion..." else "Se connecter")
        }
    }
}
