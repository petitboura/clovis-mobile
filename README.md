# Clovis Mobile

Bras armé de Clovis sur le téléphone de l'étudiant (voir `00-commun.md` et
`01-socle-app-android.md` du chantier "programme adaptatif étudiant, Partie
3"). Deux projets natifs séparés :

- `android/` — Kotlin, Jetpack Compose, deux flavors (`play` et `externe`)
- `ios/` — Swift, SwiftUI

Le "cerveau" reste dans `clovis-backend` (nouveau routeur dédié
`api/appareils_mobiles.py`). Cette app expose des capacités système et
exécute ce que le backend décide, elle ne réinvente aucune logique IA.

## État au 23/08/2026 (Lot 1 — socle)

- Auth : connexion directe à Supabase (même projet que
  `clovis-backend`/`clovis-frontend`), token envoyé en Bearer au backend.
- Canal : nouveau routeur `/api/appareils-mobiles/usage` (POST pour
  synchroniser, GET pour relire).
- Table Supabase `usage_appareil_mobile` créée en prod (`rwcyeppxfonvqbvztxyg`).
- Écran minimal : temps par app + app active aujourd'hui.
  - Android : fonctionnel via `UsageStatsManager`.
  - iOS : limite structurelle réelle, voir `ios/README.md` — pas de chiffres
    bruts par app sans compte Apple Developer + entitlement Family Controls.

## État au 23/08/2026 (Lot 2 — fichiers/dossiers désignés)

- Android : sélecteur système (`ActivityResultContracts.OpenDocumentTree`),
  permission URI persistante (`takePersistableUriPermission`), CRUD via
  `DocumentFile`/`DocumentsContract` (`DossiersDesignesRepository.kt`),
  écran `DossiersScreen.kt` (liste des dossiers désignés + navigation dans
  un dossier + créer/renommer/supprimer/déplacer).
- iOS : `UIDocumentPickerViewController` (mode `.folder`) + security-scoped
  bookmarks (`DossiersDesignesRepository.swift`), même CRUD via
  `FileManager`. Différence structurelle avec Android à noter : iOS exige
  d'ouvrir/fermer l'accès sécurisé à **chaque** opération
  (`start/stopAccessingSecurityScopedResource`), ce n'est pas une permission
  acquise une fois pour toutes côté système comme sur Android — géré via le
  wrapper `avecAcces(...)`.
- Les deux plateformes ont désormais une barre de navigation (bottom nav
  Android / TabView iOS) avec deux onglets Usage/Dossiers, en prévision des
  lots 3 à 5.
- Niveau d'accès : create/read/rename/delete/move testés au niveau du code
  sur les deux plateformes — équivalence Android/iOS non encore vérifiée
  sur appareil réel, voir avertissement ci-dessous.

## ⚠️ Important : rien de tout ça n'a été compilé ni testé

Cet environnement (sandbox Claude) n'a ni Android Studio/Gradle/SDK Android,
ni Xcode, ni accès réseau aux dépôts Maven de Google ou à l'App Store
Connect. Le code a été écrit en suivant les APIs et conventions standards,
mais **il faut l'ouvrir dans Android Studio et Xcode pour compiler,
corriger les éventuelles erreurs de build, et tester sur un appareil réel**
avant de considérer ce lot terminé — c'est une exigence explicite du
chantier (`00-commun.md`), pas juste une formalité.

## TODO avant de pouvoir tester

1. Remplacer les valeurs placeholder dans le code :
   - `SUPABASE_ANON_KEY` (Android : `SupabaseAuthClient.kt`, iOS :
     `SupabaseAuthClient.swift`) — clé anon publique du projet Supabase
     "Djiguigne AI", jamais la clé service_role.
   - `BASE_URL` / `baseURL` (Android : `ClovisApiClient.kt`, iOS :
     `ClovisApiClient.swift`) — URL Railway réelle de `clovis-backend`.
2. Ouvrir `android/` directement dans Android Studio (projet Gradle standard,
   pas d'étape supplémentaire).
3. Pour iOS, créer le projet Xcode manuellement (Xcode ne peut pas être piloté
   depuis ce sandbox) — voir `ios/README.md` pour les étapes exactes.
4. Déployer `clovis-backend` (nouveau routeur) sur Railway avant de tester la
   synchronisation.

## Prochains lots

Notifications & rappels, contrôles de session, connecteurs tiers (lots 3 à
5, communs). Accessibilité (lots 6 à 8, flavor `externe` uniquement, jamais
iOS) — pas commencés.
