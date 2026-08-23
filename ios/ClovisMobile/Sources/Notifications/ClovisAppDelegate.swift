// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// SwiftUI App n'a pas de UIApplicationDelegate par defaut -- necessaire ici
// pour les callbacks APNs (didRegisterForRemoteNotificationsWithDeviceToken),
// qui n'existent que sur UIApplicationDelegate, pas exposes autrement en
// SwiftUI pur. Branche dans ClovisMobileApp.swift via
// @UIApplicationDelegateAdaptor.
import UIKit
import UserNotifications

class ClovisAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        Task {
            do {
                try await ClovisApiClient.enregistrerPushToken(TokenPush(plateforme: "ios", token: token))
            } catch {
                // Pas grave : retente au prochain lancement (voir ClovisMobileApp.swift).
            }
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        // Simulateur (pas de vrai token APNs) ou reseau indisponible --
        // pas fatal, l'app continue de fonctionner sans push natif.
    }

    /// Affiche la notification meme si l'app est au premier plan (comportement
    /// par defaut d'iOS : rien n'apparait sans ceci quand l'app est ouverte).
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound, .list]
    }
}
