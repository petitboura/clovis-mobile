// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
//
// Meme principe que cote Android : connexion directe a Supabase (SDK
// natif), meme projet que clovis-backend/clovis-frontend
// (rwcyeppxfonvqbvztxyg). L'access_token recupere ici est envoye en
// Bearer a clovis-backend, voir ClovisApiClient.swift.
//
// TODO Bourama : remplacer SUPABASE_ANON_KEY par la vraie cle anon publique
// du projet "Djiguigne AI" (Supabase > Project Settings > API). Jamais la
// cle service_role ici.
import Foundation
import Supabase

enum SupabaseAuthClient {
    static let url = URL(string: "https://rwcyeppxfonvqbvztxyg.supabase.co")!
    static let anonKey = "A_REMPLACER_PAR_LA_CLE_ANON_PUBLIQUE"

    static let client = SupabaseClient(supabaseURL: url, supabaseKey: anonKey)

    static func connexion(email: String, motDePasse: String) async throws {
        try await client.auth.signIn(email: email, password: motDePasse)
    }

    static func accessTokenCourant() async -> String? {
        try? await client.auth.session.accessToken
    }

    static func estConnecte() async -> Bool {
        (try? await client.auth.session) != nil
    }

    static func deconnexion() async throws {
        try await client.auth.signOut()
    }
}
