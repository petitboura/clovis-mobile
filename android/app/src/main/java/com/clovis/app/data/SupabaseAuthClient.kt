// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
//
// Auth Supabase native. MEME PROJET Supabase que clovis-backend/clovis-frontend
// (voir 00-commun.md : "probablement celui deja utilise par clovis-backend, pas
// un nouveau projet separe"). L'app se connecte directement a Supabase (comme
// le fait deja clovis-frontend cote web), recupere un access_token, et l'envoie
// ensuite en Bearer a clovis-backend -- voir ClovisApiClient.kt.
//
// TODO Bourama : remplacer les deux valeurs ci-dessous par les vraies
// SUPABASE_URL / SUPABASE_ANON_KEY du projet "Djiguigne AI"
// (rwcyeppxfonvqbvztxyg), disponibles dans Supabase > Project Settings > API.
// Jamais la cle service_role ici (cote client), uniquement la cle anon
// publique -- meme regle que clovis-frontend.
package com.clovis.app.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

object SupabaseAuthClient {

    private const val SUPABASE_URL = "https://rwcyeppxfonvqbvztxyg.supabase.co"
    private const val SUPABASE_ANON_KEY = "A_REMPLACER_PAR_LA_CLE_ANON_PUBLIQUE"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
    }

    suspend fun connexion(email: String, motDePasse: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = motDePasse
        }
    }

    /** Token a envoyer en "Authorization: Bearer <token>" a clovis-backend. */
    fun accessTokenCourant(): String? = client.auth.currentAccessTokenOrNull()

    fun estConnecte(): Boolean = accessTokenCourant() != null

    suspend fun deconnexion() {
        client.auth.signOut()
    }
}
