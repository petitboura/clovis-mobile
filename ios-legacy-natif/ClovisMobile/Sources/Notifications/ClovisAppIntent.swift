// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Equivalent iOS de ClovisTileService.kt (option A du raccourci "Clovis
// joignable depuis partout", voir echange avec Bourama du 23/08/2026) :
// pas de "panneau reglages rapides" sur iOS, mais App Intents (iOS 16+)
// permet a l'etudiant d'ajouter Clovis a son ecran d'accueil (comme
// raccourci), au Centre de controle, ou de l'invoquer via Siri ("Dis Siri,
// ouvre Clovis"). Ne necessite PAS de target d'extension separe --
// contrairement au widget d'ecran d'accueil complet (WidgetKit), qui lui
// en a besoin, voir README.md pour ce dernier (pas fait ici).
import AppIntents

struct OuvrirClovisIntent: AppIntent {
    static var title: LocalizedStringResource = "Ouvrir Clovis"
    static var description = IntentDescription("Ouvre l'application Clovis.")
    static var openAppWhenRun = true

    func perform() async throws -> some IntentResult {
        .result()
    }
}

struct ClovisShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: OuvrirClovisIntent(),
            phrases: ["Ouvre \(.applicationName)"],
            shortTitle: "Ouvrir Clovis",
            systemImageName: "bubble.left.and.bubble.right"
        )
    }
}
