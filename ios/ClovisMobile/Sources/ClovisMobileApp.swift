// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
// Modifie 23/08/2026, Lot 2 : ajout de la TabView (Usage / Dossiers), voir
// MainActivity.kt cote Android pour le meme choix et sa justification.
// Etendu 23/08/2026, Lot 3 (notifications & rappels) : AppDelegate branche
// (voir ClovisAppDelegate.swift), onglet Rappels, demande d'enregistrement
// APNs a la connexion. Etendu 23/08/2026, Lot 4 (Session) et Lot 5
// (Connecteurs) : onglets correspondants.
import SwiftUI

@main
struct ClovisMobileApp: App {
    @UIApplicationDelegateAdaptor(ClovisAppDelegate.self) var appDelegate
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
                        ConnecteursScreen()
                            .tabItem { Label("Connecteurs", systemImage: "link") }
                        RappelsScreen()
                            .tabItem { Label("Rappels", systemImage: "bell") }
                    }
                    .task {
                        // Enregistre aupres d'APNs -- le token arrive de maniere
                        // asynchrone dans ClovisAppDelegate.didRegisterForRemoteNotificationsWithDeviceToken,
                        // qui l'envoie lui-meme a clovis-backend.
                        UIApplication.shared.registerForRemoteNotifications()
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
