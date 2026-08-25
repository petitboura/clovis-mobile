// Cree le 23/08/2026, Bourama : Lot 4 Partie 3 (app mobile), controles de session iOS.
//
// IMPORTANT -- limites reelles verifiees (voir 04-controles-session.md,
// critere de fin : documenter plutot que simuler) :
//
// 1. Ne pas deranger / Focus : Apple ne fournit AUCUNE API publique
//    permettant a une app tierce de basculer le Focus/DND systeme
//    directement, meme avec l'autorisation de l'utilisateur -- c'est un
//    choix de design delibere d'Apple, pas une limite technique temporaire.
//    Seul contournement existant : un Raccourci (Shortcuts/App Intents) que
//    l'etudiant configure une fois lui-meme. Ci-dessous, l'app ouvre
//    l'app Raccourcis pour que l'etudiant puisse creer ce raccourci --
//    Clovis ne peut pas le faire pour lui ni le declencher silencieusement.
//
// 2. Volume : AVAudioSession/MPVolumeView ne controlent QUE le volume
//    media de l'app elle-meme. Le volume sonnerie/notifications systeme
//    n'est pas reglable par une app tierce sur iOS, point final.
//
// Le minuteur lui, fonctionne pareil qu'Android (aucune limite systeme).
import SwiftUI
import MediaPlayer

struct ControleSessionScreen: View {
    @State private var sessionActive = false
    @State private var secoulees = 0
    private let minuteur = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 24) {
            Text("Session Clovis")
                .font(.title2.bold())

            if !sessionActive {
                VStack(alignment: .leading, spacing: 12) {
                    Label(
                        "iOS ne permet pas à Clovis de couper les notifications directement. Configure un Raccourci une seule fois pour l'automatiser toi-même.",
                        systemImage: "exclamationmark.triangle"
                    )
                    .font(.footnote)
                    .foregroundColor(.secondary)

                    Button("Configurer le raccourci Focus") {
                        if let url = URL(string: "shortcuts://") {
                            UIApplication.shared.open(url)
                        }
                    }
                    .buttonStyle(.bordered)

                    Divider()

                    Text("Volume pendant la session (média de l'app uniquement -- iOS ne permet pas de régler le volume sonnerie/notifications système)")
                        .font(.footnote)
                        .foregroundColor(.secondary)

                    VolumeSystemeSlider()
                        .frame(height: 40)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(12)
            }

            Spacer()

            if sessionActive {
                let minutes = secoulees / 60
                let secondes = secoulees % 60
                Text(String(format: "%d:%02d", minutes, secondes))
                    .font(.system(size: 48, weight: .bold, design: .rounded))
                Text("Session en cours")
                    .font(.body)
                    .foregroundColor(.secondary)
            }

            Button(action: {
                if sessionActive {
                    sessionActive = false
                } else {
                    secoulees = 0
                    sessionActive = true
                }
            }) {
                Text(sessionActive ? "Stop" : "Start")
                    .font(.headline)
                    .frame(width: 96, height: 96)
                    .background(sessionActive ? Color.red : Color.accentColor)
                    .foregroundColor(.white)
                    .clipShape(Circle())
            }

            Spacer()
        }
        .padding(24)
        .onReceive(minuteur) { _ in
            if sessionActive { secoulees += 1 }
        }
    }
}

/// Wrapper UIKit minimal pour MPVolumeView -- seul moyen d'exposer un
/// controle de volume sur iOS, et ca reste limite au volume media (voir
/// note en tete de fichier).
private struct VolumeSystemeSlider: UIViewRepresentable {
    func makeUIView(context: Context) -> MPVolumeView {
        MPVolumeView(frame: .zero)
    }
    func updateUIView(_ uiView: MPVolumeView, context: Context) {}
}
