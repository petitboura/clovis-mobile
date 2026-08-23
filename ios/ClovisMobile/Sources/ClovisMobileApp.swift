// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
// Modifie 23/08/2026, Lot 2 : ajout de la TabView (Usage / Dossiers), voir
// MainActivity.kt cote Android pour le meme choix et sa justification.
import SwiftUI

@main
struct ClovisMobileApp: App {
    @State private var connecte = false

    var body: some Scene {
        WindowGroup {
            Group {
                if connecte {
                    TabView {
                        ControleSessionScreen()
                            .tabItem { Label("Session", systemImage: "timer") }
                        UsageScreen()
                            .tabItem { Label("Usage", systemImage: "chart.bar") }
                        DossiersScreen()
                            .tabItem { Label("Dossiers", systemImage: "folder") }
                    }
                } else {
                    LoginScreen(onConnecte: { connecte = true })
                }
            }
            .task {
                connecte = await SupabaseAuthClient.estConnecte()
            }
        }
    }
}
