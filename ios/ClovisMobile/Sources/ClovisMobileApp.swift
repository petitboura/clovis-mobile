// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
import SwiftUI

@main
struct ClovisMobileApp: App {
    @State private var connecte = false

    var body: some Scene {
        WindowGroup {
            Group {
                if connecte {
                    UsageScreen()
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
