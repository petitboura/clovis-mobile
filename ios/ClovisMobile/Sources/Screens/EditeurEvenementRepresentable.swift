// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
// EKEventEditViewController est UIKit -- pont necessaire pour l'afficher
// depuis SwiftUI (pas d'equivalent SwiftUI natif fourni par Apple).
import EventKit
import EventKitUI
import SwiftUI

struct EditeurEvenementRepresentable: UIViewControllerRepresentable {
    let titre: String
    let debut: Date
    let fin: Date
    var onTermine: () -> Void

    func makeUIViewController(context: Context) -> EKEventEditViewController {
        let controleur = RappelsNatifs.creerControleurEvenement(titre: titre, debut: debut, fin: fin)
        controleur.editViewDelegate = context.coordinator
        return controleur
    }

    func updateUIViewController(_ uiViewController: EKEventEditViewController, context: Context) {}

    func makeCoordinator() -> Coordinateur { Coordinateur(onTermine: onTermine) }

    class Coordinateur: NSObject, EKEventEditViewDelegate {
        let onTermine: () -> Void
        init(onTermine: @escaping () -> Void) { self.onTermine = onTermine }

        func eventEditViewController(
            _ controller: EKEventEditViewController,
            didCompleteWith action: EKEventEditViewAction
        ) {
            onTermine()
        }
    }
}
