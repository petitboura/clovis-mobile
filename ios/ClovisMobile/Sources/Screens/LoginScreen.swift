// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle iOS.
import SwiftUI

struct LoginScreen: View {
    var onConnecte: () -> Void

    @State private var email = ""
    @State private var motDePasse = ""
    @State private var enCours = false
    @State private var erreur: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Clovis")
                .font(.largeTitle.bold())

            TextField("Email", text: $email)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)
                .keyboardType(.emailAddress)

            SecureField("Mot de passe", text: $motDePasse)
                .textFieldStyle(.roundedBorder)

            if let erreur {
                Text(erreur).foregroundColor(.red)
            }

            Button {
                erreur = nil
                enCours = true
                Task {
                    do {
                        try await SupabaseAuthClient.connexion(email: email, motDePasse: motDePasse)
                        onConnecte()
                    } catch {
                        erreur = "Connexion impossible, vérifie tes identifiants."
                    }
                    enCours = false
                }
            } label: {
                Text(enCours ? "Connexion..." : "Se connecter")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(enCours || email.isEmpty || motDePasse.isEmpty)

            Spacer()
        }
        .padding(24)
    }
}
