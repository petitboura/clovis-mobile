// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Calendrier : EventKit, comme demande par 03-notifications-rappels.md.
// EKEventEditViewController (UI native de confirmation, comme
// Intent.ACTION_INSERT cote Android) plutot qu'un insert silencieux via
// EKEventStore.save() -- meme choix que cote Android et pour la meme
// raison (l'etudiant voit et confirme avant tout ajout reel a son
// calendrier personnel).
//
// Ouverture d'app : confirme en recherche, PAS d'equivalent iOS a
// PackageManager.getLaunchIntentForPackage. Seule une app qui expose
// explicitement un URL scheme (ex: "whatsapp://") ou un Universal Link
// peut etre ouverte, et seulement si Clovis connait ce schema a l'avance
// -- aucune liste exhaustive et fiable des schemas de toutes les apps
// n'existe, a construire au cas par cas si Bourama fournit une liste
// d'apps a supporter.
import EventKit
import EventKitUI
import UIKit

enum RappelsNatifs {

    /// Presente l'UI native EventKit de creation d'evenement (l'etudiant
    /// confirme avant tout ajout reel). A appeler depuis un contexte SwiftUI
    /// via UIViewControllerRepresentable (voir RappelsScreen.swift).
    static func creerControleurEvenement(
        titre: String,
        debut: Date,
        fin: Date
    ) -> EKEventEditViewController {
        let store = EKEventStore()
        let evenement = EKEvent(eventStore: store)
        evenement.title = titre
        evenement.startDate = debut
        evenement.endDate = fin

        let controleur = EKEventEditViewController()
        controleur.eventStore = store
        controleur.event = evenement
        return controleur
    }

    /// `schema` ex: "whatsapp://" -- doit etre déclaré dans
    /// LSApplicationQueriesSchemes (Info.plist) pour que canOpenURL()
    /// fonctionne, voir Info.plist.
    static func ouvrirApp(schema: String) -> Bool {
        guard let url = URL(string: schema), UIApplication.shared.canOpenURL(url) else {
            return false
        }
        UIApplication.shared.open(url)
        return true
    }
}
