// Cree le 23/08/2026, Bourama : Lot 2 Partie 3 (app mobile), fichiers/dossiers designes, iOS.
//
// Meme principe que DossiersScreen.kt cote Android : deux niveaux (liste des
// dossiers designes, puis navigation a l'interieur d'un dossier via une pile
// en memoire). UIDocumentPickerViewController n'a pas d'equivalent SwiftUI
// natif -- on le passe par UIViewControllerRepresentable.
import SwiftUI
import UniformTypeIdentifiers

struct DossiersScreen: View {
    @State private var dossiersDesignes: [DossierDesigne] = DossiersDesignesRepository.listerDossiersDesignes()
    @State private var pile: [(url: URL, nom: String)] = []
    @State private var contenu: [ElementDossier] = []
    @State private var afficherSelecteur = false
    @State private var afficherNouveauDossier = false
    @State private var afficherNouveauFichier = false
    @State private var elementARenommer: ElementDossier?
    @State private var elementASupprimer: ElementDossier?
    @State private var nomSaisi = ""
    @State private var erreur: String?

    private func rafraichirContenu() {
        guard let courant = pile.last else { return }
        contenu = DossiersDesignesRepository.listerContenu(courant.url)
    }

    var body: some View {
        Group {
            if let courant = pile.last {
                vueContenuDossier(courant: courant)
            } else {
                vueRacine
            }
        }
        .sheet(isPresented: $afficherSelecteur) {
            SelecteurDossier { url in
                DossiersDesignesRepository.ajouterDossierDesigne(url: url)
                dossiersDesignes = DossiersDesignesRepository.listerDossiersDesignes()
            }
        }
    }

    private var vueRacine: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Dossiers désignés").font(.title2.bold())
            Text("Clovis ne peut lire ou modifier que les dossiers que tu choisis explicitement ici.")
                .font(.subheadline)
                .foregroundColor(.secondary)

            Button {
                afficherSelecteur = true
            } label: {
                Label("Désigner un dossier", systemImage: "plus")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            if dossiersDesignes.isEmpty {
                Text("Aucun dossier désigné pour l'instant.")
                    .foregroundColor(.secondary)
            }

            List {
                ForEach(dossiersDesignes) { dossier in
                    HStack {
                        Image(systemName: "folder")
                        Text(dossier.url.lastPathComponent)
                        Spacer()
                        Button {
                            DossiersDesignesRepository.retirerDossierDesigne(id: dossier.id)
                            dossiersDesignes = DossiersDesignesRepository.listerDossiersDesignes()
                        } label: {
                            Image(systemName: "xmark.circle")
                        }
                        .buttonStyle(.plain)
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        pile = [(url: dossier.url, nom: dossier.url.lastPathComponent)]
                        rafraichirContenu()
                    }
                }
            }
            .listStyle(.plain)
        }
        .padding(24)
    }

    private func vueContenuDossier(courant: (url: URL, nom: String)) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Button {
                    pile.removeLast()
                    if !pile.isEmpty { rafraichirContenu() }
                } label: {
                    Image(systemName: "chevron.left")
                }
                Text(pile.map { $0.nom }.joined(separator: " / "))
                    .font(.headline)
                    .lineLimit(1)
            }

            HStack(spacing: 12) {
                Button {
                    nomSaisi = ""
                    afficherNouveauDossier = true
                } label: {
                    Label("Dossier", systemImage: "folder.badge.plus")
                }
                Button {
                    nomSaisi = ""
                    afficherNouveauFichier = true
                } label: {
                    Label("Fichier", systemImage: "doc.badge.plus")
                }
            }
            .buttonStyle(.bordered)

            if let erreur {
                Text(erreur).foregroundColor(.red).font(.footnote)
            }

            if contenu.isEmpty {
                Text("Dossier vide.").foregroundColor(.secondary)
            }

            List {
                ForEach(contenu) { element in
                    HStack {
                        Image(systemName: element.estDossier ? "folder" : "doc")
                        VStack(alignment: .leading) {
                            Text(element.nom)
                            if !element.estDossier {
                                Text("\(element.tailleOctets / 1024) Ko")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        Spacer()
                        Button {
                            elementARenommer = element
                            nomSaisi = element.nom
                        } label: {
                            Image(systemName: "pencil")
                        }
                        .buttonStyle(.plain)
                        Button {
                            elementASupprimer = element
                        } label: {
                            Image(systemName: "trash")
                        }
                        .buttonStyle(.plain)
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        if element.estDossier {
                            pile.append((url: element.url, nom: element.nom))
                            rafraichirContenu()
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
        .padding(24)
        .alert("Nouveau dossier", isPresented: $afficherNouveauDossier) {
            TextField("Nom du dossier", text: $nomSaisi)
            Button("Annuler", role: .cancel) {}
            Button("Créer") {
                erreur = DossiersDesignesRepository.creerSousDossier(dansParent: courant.url, nom: nomSaisi) ? nil : "Impossible de créer ce dossier."
                rafraichirContenu()
            }
        }
        .alert("Nouveau fichier", isPresented: $afficherNouveauFichier) {
            TextField("Nom du fichier (avec extension)", text: $nomSaisi)
            Button("Annuler", role: .cancel) {}
            Button("Créer") {
                erreur = DossiersDesignesRepository.creerFichier(dansParent: courant.url, nom: nomSaisi) ? nil : "Impossible de créer ce fichier."
                rafraichirContenu()
            }
        }
        .alert("Renommer", isPresented: Binding(get: { elementARenommer != nil }, set: { if !$0 { elementARenommer = nil } })) {
            TextField("Nouveau nom", text: $nomSaisi)
            Button("Annuler", role: .cancel) { elementARenommer = nil }
            Button("Renommer") {
                if let element = elementARenommer {
                    erreur = DossiersDesignesRepository.renommer(element.url, nouveauNom: nomSaisi) ? nil : "Impossible de renommer."
                    rafraichirContenu()
                }
                elementARenommer = nil
            }
        }
        .alert(
            elementASupprimer.map { "Supprimer « \($0.nom) » ?" } ?? "",
            isPresented: Binding(get: { elementASupprimer != nil }, set: { if !$0 { elementASupprimer = nil } })
        ) {
            Button("Annuler", role: .cancel) { elementASupprimer = nil }
            Button("Supprimer", role: .destructive) {
                if let element = elementASupprimer {
                    erreur = DossiersDesignesRepository.supprimer(element.url) ? nil : "Impossible de supprimer."
                    rafraichirContenu()
                }
                elementASupprimer = nil
            }
        } message: {
            Text(elementASupprimer?.estDossier == true ? "Le dossier et tout son contenu seront supprimés." : "Le fichier sera supprimé définitivement.")
        }
    }
}

/// Wrapper UIKit -> SwiftUI pour UIDocumentPickerViewController en mode
/// selection de dossier (SwiftUI n'a pas d'equivalent natif a ce jour).
private struct SelecteurDossier: UIViewControllerRepresentable {
    var onChoisi: (URL) -> Void

    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.folder])
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(onChoisi: onChoisi) }

    class Coordinator: NSObject, UIDocumentPickerDelegate {
        let onChoisi: (URL) -> Void
        init(onChoisi: @escaping (URL) -> Void) { self.onChoisi = onChoisi }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            if let url = urls.first { onChoisi(url) }
        }
    }
}
