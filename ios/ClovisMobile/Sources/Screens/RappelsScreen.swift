// Cree le 23/08/2026, Bourama : Lot 3 Partie 3 (app mobile), notifications & rappels.
//
// Meme role que RappelsScreen.kt cote Android : chaque capacite native
// declenchable manuellement pour verifier sur appareil reel avant de
// considerer le lot termine. Pas d'equivalent iOS a l'alerte plein ecran
// Android -- voir note en tete de NotificationsNatives.swift, donc pas de
// bouton "alerte prioritaire plein ecran" ici, seulement time-sensitive.
import SwiftUI

struct RappelsScreen: View {
    @State private var autorisationAccordee = false
    @State private var afficherEditeurEvenement = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Notifications & rappels")
                    .font(.title2.bold())
                Text("Écran de test du Lot 3 -- les rappels réels arriveront via Clovis, ceci sert à vérifier que chaque capacité fonctionne sur cet appareil.")
                    .font(.footnote)
                    .foregroundColor(.secondary)

                if !autorisationAccordee {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Clovis n'a pas encore le droit d'envoyer des notifications.")
                        Button("Autoriser les notifications") {
                            Task { autorisationAccordee = await NotificationsNatives.demanderAutorisation() }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .padding(12)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(12)
                }

                Group {
                    Text("Notification classique").font(.headline)
                    Button("Envoyer une notification de test") {
                        NotificationsNatives.afficherNotificationTest(
                            titre: "Rappel Clovis", corps: "Ceci est une notification de test.", prioritaire: false
                        )
                    }
                    .buttonStyle(.bordered)
                    .disabled(!autorisationAccordee)
                }

                Group {
                    Text("Notification prioritaire (time-sensitive)").font(.headline)
                    Text("Traverse le mode Ne pas déranger, mais reste une notification standard -- iOS n'a pas d'équivalent à l'alerte plein écran Android (réservé à CallKit/téléphonie, voir README).")
                        .font(.footnote)
                        .foregroundColor(.secondary)
                    Button("Envoyer une notification prioritaire de test") {
                        NotificationsNatives.afficherNotificationTest(
                            titre: "Rappel important", corps: "Ceci est une notification prioritaire de test.", prioritaire: true
                        )
                    }
                    .buttonStyle(.bordered)
                    .disabled(!autorisationAccordee)
                }

                Group {
                    Text("Rappel programmé (équivalent alarme)").font(.headline)
                    Button("Programmer un rappel de test dans 1 minute") {
                        NotificationsNatives.programmerRappel(
                            titre: "Rappel Clovis",
                            corps: "Rappel programmé de test.",
                            date: Date().addingTimeInterval(60)
                        )
                    }
                    .buttonStyle(.bordered)
                    .disabled(!autorisationAccordee)
                }

                Group {
                    Text("Calendrier").font(.headline)
                    Button("Ajouter un événement de test") {
                        afficherEditeurEvenement = true
                    }
                    .buttonStyle(.bordered)
                }
            }
            .padding(24)
        }
        .task {
            autorisationAccordee = await NotificationsNatives.autorisationAccordee()
        }
        .sheet(isPresented: $afficherEditeurEvenement) {
            EditeurEvenementRepresentable(
                titre: "Événement Clovis (test)",
                debut: Date(),
                fin: Date().addingTimeInterval(3600),
                onTermine: { afficherEditeurEvenement = false }
            )
        }
    }
}
