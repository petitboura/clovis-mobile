// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
//
// Meme canal dedie que cote Android, voir
// clovis-backend/api/appareils_mobiles.py.
//
// TODO Bourama : remplacer baseURL par l'URL Railway reelle de clovis-backend.
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
}
