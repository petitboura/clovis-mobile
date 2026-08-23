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

## État au 23/08/2026 (Lot 5 — connecteurs tiers, Notion)

- Architecture retenue : l'app mobile appelle `clovis-backend`, qui pilote
  l'OAuth et les tokens (réutilise `connexions/notion.py`, déjà utilisé par
  le chat) — pas de stockage de token côté téléphone.
- **Correction importante en amont** : le connecteur Notion existant portait
  un `client_name="Djiguigne"` et un `redirect_uri` sur un domaine
  `djiguigne.vercel.app` — fuite de marque, contraire à la contrainte stricte
  du chantier. Corrigé côté `clovis-backend` (nouvel enregistrement DCR sous
  "Clovis", `URL_RETOUR_APP` migré vers `classgpt-frontend.vercel.app`).
  L'ancienne connexion Notion de Bourama a été supprimée (à refaire).
- Flow OAuth mobile : `clovismobile://oauth-callback` enregistré comme
  redirect_uri supplémentaire auprès de Notion (DCR accepte plusieurs URIs),
  dédié au mobile — aucun changement nécessaire côté frontend web.
  - Android : Custom Tabs + `OAuthCallbackActivity.kt` (intent-filter sur le
    schéma) + `RetourOAuth` (SharedFlow) pour relayer code/state à
    `ConnecteursScreen.kt`.
  - iOS : `ASWebAuthenticationSession` (callbackURLScheme, pas besoin
    d'entrée Info.plist pour ce mécanisme précis) dans `ConnecteursScreen.swift`.
- Backend : nouveaux endpoints sous `/api/appareils-mobiles/connecteurs/notion/`
  (`demarrer`, `finaliser`, `statut`, `rechercher`) dans `appareils_mobiles.py`.
  `rechercher` appelle directement l'API REST Notion (`/v1/search`), pas le
  MCP Notion (pensé pour l'agent de chat, pas pour un appel mobile simple).
- Critère de fin (connecteur fonctionnel de bout en bout) : code en place
  sur les trois dépôts, jamais testé sur appareil réel — voir avertissement.

## ⚠️ Important : rien de tout ça n'a été compilé ni testé

Cet environnement (sandbox Claude) n'a ni Android Studio/Gradle/SDK Android,
ni Xcode, ni accès réseau aux dépôts Maven de Google ou à l'App Store
Connect. Le code a été écrit en suivant les APIs et conventions standards,
mais **il faut l'ouvrir dans Android Studio et Xcode pour compiler,
corriger les éventuelles erreurs de build, et tester sur un appareil réel**
avant de considérer ce lot terminé — c'est une exigence explicite du
chantier (`00-commun.md`), pas juste une formalité.

## État au 23/08/2026 (Lot 4 — contrôles de session)

Construit par-dessus le Lot 1 non compilé/non testé (décision explicite de
Bourama : continuer, tout tester ensemble plus tard).

- **Android** : nouvel écran `ControleSessionScreen.kt` + repository
  `ControleSessionRepository.kt`. Bascule DND (`INTERRUPTION_FILTER_ALARMS`,
  à ajuster si Bourama veut un filtre différent) et coupe volume
  sonnerie/notifications, via la permission spéciale "Accès à la Politique de
  Notification" (`ACCESS_NOTIFICATION_POLICY`, ajoutée au manifeste commun).
  État initial capturé avant modification et restauré exactement à l'arrêt.
  Onglet "Session" ajouté à la barre de navigation existante (Session /
  Usage / Dossiers), en premier onglet.
- **iOS** : nouvel écran `ControleSessionScreen.swift`. Le minuteur
  fonctionne pareil qu'Android. **DND/Focus et volume système : impossibles
  pour une app tierce sur iOS**, vérifié et documenté plutôt que simulé (voir
  commentaire en tête du fichier) — DND propose seulement d'ouvrir l'app
  Raccourcis pour que l'étudiant configure lui-même un automation Focus ;
  volume limité au volume média de l'app via `MPVolumeView`.
- **Limite connue, non traitée ici** : si l'app Android est tuée par l'OS
  pendant une session active, le DND/volume ne seront pas restaurés
  automatiquement (pas de foreground service). À traiter dans une prochaine
  session si Bourama le juge nécessaire — décision produit, pas prise seul.

## TODO avant de pouvoir tester

1. Ouvrir `android/` directement dans Android Studio (projet Gradle standard,
   pas d'étape supplémentaire).
2. Pour iOS, créer le projet Xcode manuellement (Xcode ne peut pas être piloté
   depuis ce sandbox) — voir `ios/README.md` pour les étapes exactes.
3. Déployer `clovis-backend` (nouveau routeur) sur Railway avant de tester la
   synchronisation.

## Prochains lots

Notifications & rappels (lot 3, commun) — pas fait, jugé non nécessaire pour
débloquer les lots 6/7 (voir section Lot 6 ci-dessous).
Distribution hors store (lot 8, flavor `externe` uniquement) — pas commencé.

## Lot 6 — Service d'accessibilité (23/08/2026)

Flavor `externe` uniquement, comme prévu (`06-service-accessibilite.md`).
Lot 3 (notifications & rappels) n'a pas été fait — vérifié non nécessaire
pour ce lot précis, aucune dépendance fonctionnelle réelle entre les deux.

- `ServiceAccessibiliteClovis` : lecture seule (aucune action, portée
  stricte du Lot 6), journalise ce qu'il observe.
- Écran de consentement en langage simple, affiché avant l'écran système.
- Isolation garantie au niveau des sources compilées : le flavor `play` ne
  contient aucune ligne de code d'accessibilité (stub vide dans
  `src/play/.../ModuleAccessibilite.kt`), pas juste un interrupteur runtime.
- Pas testé sur appareil réel (pas d'outillage Android dans ce sandbox,
  voir plus haut) — obligatoire avant de considérer ce lot terminé,
  notamment vérifier la lecture sur au moins deux apps tierces différentes.

Reste à faire avant le Lot 7 : Lot 3 (si besoin plus tard), puis Lot 7
(actions pilotées) dépend explicitement du Lot 6.

## Lot 7 — Actions pilotées & fiabilité multi-app (23/08/2026)

Flavor `externe` uniquement, construit sur le Lot 6 (`07-actions-pilotees.md`).

- `AppsAutorisees.kt` : liste persistée (SharedPreferences) des apps
  explicitement autorisées par l'étudiant, une par une (switch par app dans
  l'onglet Accessibilité) — aucune action n'est jamais tentée sur une app
  non listée ici, vérifié à chaque appel.
- `ExecuteurActions.kt` : recherche un élément dans l'arbre par texte/
  description (profondeur plafonnée, un seul parcours), puis `ACTION_CLICK`
  ou `ACTION_SET_TEXT` via `AccessibilityNodeInfo.performAction`. Pas de
  geste tactile brut (`dispatchGesture`) — pas nécessaire pour ce lot, donc
  aucun changement de capacité dans `accessibility_service_config.xml`.
- Comportement de repli (règle "ne jamais deviner" appliquée à l'agent) :
  un seul essai par appel, jamais de boucle de réessai. Élément introuvable,
  app non autorisée, ou action refusée par le système → échec immédiat avec
  message clair, journalisé dans `JournalActions.kt` (visible dans l'onglet
  Accessibilité, section "Dernières tentatives d'action").
- Panneau de test manuel ajouté à `EcranAccessibilite.kt` (champ "élément
  ciblé" + "texte à saisir" + boutons Cliquer/Saisir) — nécessaire pour
  vérifier le critère de fin sur appareil réel : au moins une action de bout
  en bout fiable, et le repli vérifié en cassant volontairement le scénario
  (app fermée, élément renommé/déplacé, app non autorisée).
- Pas testé sur appareil réel (même limite que tous les lots précédents,
  voir avertissement plus haut).

## Lot 8 — Distribution hors store (23/08/2026)

Flavor `externe` uniquement, en parallèle du Lot 6 comme prévu par le
document (dépendance : "au moins le Lot 6 terminé", ✓).

- **Signature** : configurée pour lire une clé locale jamais committée
  (`android/keystore.properties`, gitignored) — voir
  `android/README-SIGNATURE.md` pour la générer. Sans ce fichier, le build
  `externe` compile quand même mais n'est pas signé (utile pour développer
  sans avoir encore la clé).
- **Vérification de mise à jour** : l'app interroge l'API publique GitHub
  Releases (`petitboura/clovis-mobile`), compare les versions numériquement,
  et affiche une bannière avec un bouton "Télécharger" si une version plus
  récente existe. Décision prise avec Bourama : hébergement sur GitHub
  Releases (déjà en place, pas de nouvelle infrastructure).
- **Page d'installation** pour l'étudiant qui sideload : `docs/installation.md`.
- Nettoyage au passage : dépendance `material-icons-extended` qui était
  déclarée deux fois dans `build.gradle.kts` (résidu de deux lots parallèles).

Pas testé sur appareil réel (même limite d'outillage que les lots
précédents). Pour publier une vraie release : suivre
`android/README-SIGNATURE.md` (générer la clé une fois, puis à chaque
version : incrémenter versionCode/versionName, générer l'APK signé, créer
la Release GitHub avec l'APK en pièce jointe).

## Tous les lots de la Partie 3 ont maintenant du code en place (23/08/2026)

Lots 1, 2, 4, 5, 6, 7, 8 : faits. Lot 3 (notifications & rappels) reste le
seul non fait — vérifié non bloquant pour les lots 6/7/8, mais reste à
faire pour que le programme soit complet. **Rien de tout ça n'a été
compilé ni testé sur un appareil réel**, voir l'avertissement plus haut :
c'est la prochaine étape avant de considérer la Partie 3 terminée.
