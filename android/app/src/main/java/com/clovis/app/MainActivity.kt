// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
package com.clovis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.clovis.app.data.SupabaseAuthClient
import com.clovis.app.ui.screens.LoginScreen
import com.clovis.app.ui.screens.UsageScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var connecte by remember { mutableStateOf(SupabaseAuthClient.estConnecte()) }
                    if (connecte) {
                        UsageScreen()
                    } else {
                        LoginScreen(onConnecte = { connecte = true })
                    }
                }
            }
        }
    }
}
