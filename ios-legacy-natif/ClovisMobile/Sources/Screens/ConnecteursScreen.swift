// Cree le 23/08/2026, Bourama : Lot 5 Partie 3 (app mobile), connecteurs tiers, iOS.
//
// ASWebAuthenticationSession avec callbackURLScheme="clovismobile" :
// contrairement a un deep link classique, ce mecanisme n'a PAS besoin
// d'enregistrer le schema dans Info.plist (CFBundleURLTypes) -- la session
// intercepte elle-meme la navigation vers clovismobile://oauth-callback
// dans sa propre vue web ephemere, avant que l'OS n'essaie de la router.
// Equivalent Android : Custom Tabs + OAuthCallbackActivity.kt (intent-filter).
import SwiftUI
import AuthenticationServices

@MainActor
private final class SessionConnexionNotion: NSObject, ASWebAuthenticationPresentationContextProviding, ObservableObject {
    private var session: ASWebAuthenticationSession?

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first { $0.isKeyWindow } ?? ASPresentationAnchor()
    }

    /// Lance le flux, renvoie (code, state) extraits de l'URL de callback.
    func lancer(urlAutorisation: String) async throws -> (code: String, state: String) {
        guard let url = URL(string: urlAutorisation) else {
            throw NSError(domain: "SessionConnexionNotion", code: 1, userInfo: [NSLocalizedDescriptionKey: "URL d'autorisation invalide."])
        }
        return try await withCheckedThrowingContinuation { continuation in
            let session = ASWebAuthenticationSession(url: url, callbackURLScheme: "clovismobile") { callbackURL, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                guard
                    let callbackURL,
                    let composants = URLComponents(url: callbackURL, resolvingAgainstBaseURL: false),
                    let code = composants.queryItems?.first(where: { $0.name == "code" })?.value,
                    let state = composants.queryItems?.first(where: { $0.name == "state" })?.value
                else {
                    continuation.resume(throwing: NSError(domain: "SessionConnexionNotion", code: 2, userInfo: [NSLocalizedDescriptionKey: "Réponse de connexion incomplète."]))
                    return
                }
                continuation.resume(returning: (code, state))
            }
            session.presentationContextProvider = self
            session.prefersEphemeralWebBrowserSession = true
            self.session = session
            session.start()
        }
    }
}

struct ConnecteursScreen: View {
    @StateObject private var sessionNotion = SessionConnexionNotion()
    @State private var notionConnecte: Bool?
    @State private var chargementConnexion = false
    @State private var erreur: String?
    @State private var requeteRecherche = ""
    @State private var resultats: [ResultatNotion] = []
    @State private var rechercheEnCours = false

    private func rafraichirStatut() async {
        do {
            notionConnecte = try await ClovisApiClient.statutNotion().connecte
        } catch {
            erreur = "Impossible de vérifier le statut Notion."
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Connecteurs tiers").font(.title2.bold())
            Text("Clovis peut utiliser tes comptes sur d'autres apps, uniquement via leur connexion officielle.")
                .font(.subheadline)
                .foregroundColor(.secondary)

            if let erreur {
                Text(erreur).foregroundColor(.red).font(.footnote)
            }

            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("Notion").font(.headline)
                    Spacer()
                    if notionConnecte == true {
                        Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
                    } else if notionConnecte == nil {
                        ProgressView()
                    }
                }

                if notionConnecte == false {
                    Button {
                        Task {
                            chargementConnexion = true
                            erreur = nil
                            do {
                                let reponse = try await ClovisApiClient.demarrerConnexionNotion()
                                let (code, state) = try await sessionNotion.lancer(urlAutorisation: reponse.url_autorisation)
                                _ = try await ClovisApiClient.finaliserConnexionNotion(code: code, state: state)
                                await rafraichirStatut()
                            } catch {
                                erreur = "Connexion Notion échouée."
                            }
                            chargementConnexion = false
                        }
                    } label: {
                        Text(chargementConnexion ? "Connexion en cours…" : "Connecter Notion")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(chargementConnexion)
                }

                if notionConnecte == true {
                    TextField("Rechercher dans Notion", text: $requeteRecherche)
                        .textFieldStyle(.roundedBorder)

                    Button {
                        Task {
                            rechercheEnCours = true
                            erreur = nil
                            do {
                                resultats = try await ClovisApiClient.rechercherNotion(requeteRecherche).resultats
                            } catch {
                                erreur = "Recherche Notion impossible."
                            }
                            rechercheEnCours = false
                        }
                    } label: {
                        Label(rechercheEnCours ? "Recherche…" : "Rechercher", systemImage: "magnifyingglass")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .disabled(rechercheEnCours)

                    List(resultats) { resultat in
                        Text("\(resultat.type) — \(resultat.id)").font(.caption)
                    }
                    .listStyle(.plain)
                }
            }
            .padding(16)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color(.secondarySystemBackground)))

            Spacer()
        }
        .padding(24)
        .task { await rafraichirStatut() }
    }
}
