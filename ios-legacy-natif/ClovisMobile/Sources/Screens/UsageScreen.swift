// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
//
// IMPORTANT -- limite reelle verifiee (voir ios/README.md pour le detail) :
// contrairement a Android (UsageStatsManager, chiffres bruts directement
// lisibles par notre code), iOS n'expose PAS le temps par app en chiffres
// bruts a une app tierce. Il faut :
//   1. L'autorisation Family Controls (.individual), qui necessite un compte
//      Apple Developer Program actif -- Bourama n'en a pas encore (voir
//      00-commun.md).
//   2. Un target d'extension separe (DeviceActivityReportExtension) rendu
//      par Apple, ou l'app hote ne peut PAS lire les noms d'app ni les
//      durees exactes comme donnees -- seulement afficher le rendu visuel
//      qu'Apple fournit, dans un cadre prive/protege.
// Consequence concrete pour ce Lot 1 : la synchronisation vers
// clovis-backend (meme mecanisme que Android) n'est PAS possible avec des
// chiffres exacts par app tant que ce qui precede n'est pas en place. Ecran
// ci-dessous : demande l'autorisation, explique la limite a l'etudiant,
// affiche le rendu Apple (DeviceActivityReport) une fois le target ajoute
// dans Xcode -- PAS encore fait ici, voir README.
import SwiftUI
import FamilyControls

struct UsageScreen: View {
    @State private var centre = AuthorizationCenter.shared
    @State private var autorise = false
    @State private var erreurAutorisation: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Temps d'écran aujourd'hui")
                .font(.title2.bold())

            if !autorise {
                Text("Clovis a besoin de ton autorisation Écran et temps d'utilisation pour afficher ton temps par app.")
                    .font(.body)

                Text("Note : sur iPhone, Apple ne permet pas à Clovis de lire les chiffres exacts par app comme sur Android. Le détail s'affichera dans un cadre fourni directement par Apple, pas par Clovis.")
                    .font(.footnote)
                    .foregroundColor(.secondary)

                if let erreurAutorisation {
                    Text(erreurAutorisation).foregroundColor(.red).font(.footnote)
                }

                Button("Autoriser") {
                    Task {
                        do {
                            try await centre.requestAuthorization(for: .individual)
                            autorise = centre.authorizationStatus == .approved
                        } catch {
                            erreurAutorisation = "Autorisation refusée ou indisponible."
                        }
                    }
                }
                .buttonStyle(.borderedProminent)
            } else {
                // TODO Bourama / prochaine session avec Xcode : une fois le
                // target "ClovisMobileReportExtension" (DeviceActivityReportExtension)
                // ajoute au projet, decommenter et brancher ici :
                //
                // DeviceActivityReport(
                //     .init(rawValue: "UsageQuotidien"),
                //     filter: DeviceActivityFilter(
                //         segment: .daily(during: Calendar.current.dateInterval(of: .day, for: .now)!)
                //     )
                // )
                Text("Autorisation accordée. Le rendu détaillé nécessite le target d'extension à ajouter dans Xcode (voir README).")
                    .font(.body)
            }

            Spacer()
        }
        .padding(24)
        .task {
            autorise = centre.authorizationStatus == .approved
        }
    }
}
