// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
//
// Meme canal dedie que cote Android, voir
// clovis-backend/api/appareils_mobiles.py.
import Foundation

struct EntreeUsage: Codable {
    let nom_app: String
    let date: String
    let duree_secondes: Int
}

struct SynchronisationUsage: Codable {
    let plateforme: String
    let entrees: [EntreeUsage]
}

struct LigneUsage: Codable {
    let plateforme: String
    let nom_app: String
    let date: String
    let duree_secondes: Int
}

struct ReponseUsage: Codable {
    let usage: [LigneUsage]
}

// MARK: - Lot 5 : connecteurs tiers (Notion)

struct UrlAutorisationNotion: Codable {
    let url_autorisation: String
}

struct FinalisationNotion: Codable {
    let code: String
    let state: String
}

struct ReponseFinalisationNotion: Codable {
    let connecte: Bool
    let espace: String?
}

struct StatutNotion: Codable {
    let connecte: Bool
}

struct ResultatNotion: Codable, Identifiable {
    let id: String
    let type: String
    let url: String?
}

struct ReponseRechercheNotion: Codable {
    let resultats: [ResultatNotion]
}

// MARK: - Lot 3 : notifications push natives

struct TokenPush: Codable {
    let plateforme: String
    let token: String
}

enum ClovisApiClient {
    static let baseURL = "https://clovis-backend-production.up.railway.app"

    private static func requeteAuthentifiee(_ url: URL, methode: String) async throws -> URLRequest {
        guard let token = await SupabaseAuthClient.accessTokenCourant() else {
            throw NSError(domain: "ClovisApiClient", code: 401, userInfo: [
                NSLocalizedDescriptionKey: "Pas de session Supabase active."
            ])
        }
        var requete = URLRequest(url: url)
        requete.httpMethod = methode
        requete.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        requete.setValue("application/json", forHTTPHeaderField: "Content-Type")
        return requete
    }

    static func synchroniserUsage(_ payload: SynchronisationUsage) async throws {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/usage")!
        var requete = try await requeteAuthentifiee(url, methode: "POST")
        requete.httpBody = try JSONEncoder().encode(payload)
        _ = try await URLSession.shared.data(for: requete)
    }

    static func obtenirUsage(jours: Int = 7) async throws -> ReponseUsage {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/usage?jours=\(jours)")!
        let requete = try await requeteAuthentifiee(url, methode: "GET")
        let (data, _) = try await URLSession.shared.data(for: requete)
        return try JSONDecoder().decode(ReponseUsage.self, from: data)
    }

    static func demarrerConnexionNotion() async throws -> UrlAutorisationNotion {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/connecteurs/notion/demarrer")!
        let requete = try await requeteAuthentifiee(url, methode: "POST")
        let (data, _) = try await URLSession.shared.data(for: requete)
        return try JSONDecoder().decode(UrlAutorisationNotion.self, from: data)
    }

    static func finaliserConnexionNotion(code: String, state: String) async throws -> ReponseFinalisationNotion {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/connecteurs/notion/finaliser")!
        var requete = try await requeteAuthentifiee(url, methode: "POST")
        requete.httpBody = try JSONEncoder().encode(FinalisationNotion(code: code, state: state))
        let (data, _) = try await URLSession.shared.data(for: requete)
        return try JSONDecoder().decode(ReponseFinalisationNotion.self, from: data)
    }

    static func statutNotion() async throws -> StatutNotion {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/connecteurs/notion/statut")!
        let requete = try await requeteAuthentifiee(url, methode: "GET")
        let (data, _) = try await URLSession.shared.data(for: requete)
        return try JSONDecoder().decode(StatutNotion.self, from: data)
    }

    static func rechercherNotion(_ requeteTexte: String) async throws -> ReponseRechercheNotion {
        var composants = URLComponents(string: "\(baseURL)/api/appareils-mobiles/connecteurs/notion/rechercher")!
        composants.queryItems = [URLQueryItem(name: "q", value: requeteTexte)]
        let requete = try await requeteAuthentifiee(composants.url!, methode: "GET")
        let (data, _) = try await URLSession.shared.data(for: requete)
        return try JSONDecoder().decode(ReponseRechercheNotion.self, from: data)
    }

    // Ajoute le 23/08/2026, Lot 3 (notifications & rappels) : enregistre le
    // token APNs aupres de clovis-backend, voir didRegisterForRemoteNotificationsWithDeviceToken
    // dans ClovisAppDelegate.swift (appele a chaque lancement, le token
    // APNs peut changer -- meme logique que le token FCM cote Android).
    static func enregistrerPushToken(_ payload: TokenPush) async throws {
        let url = URL(string: "\(baseURL)/api/appareils-mobiles/push-token")!
        var requete = try await requeteAuthentifiee(url, methode: "POST")
        requete.httpBody = try JSONEncoder().encode(payload)
        _ = try await URLSession.shared.data(for: requete)
    }
}
