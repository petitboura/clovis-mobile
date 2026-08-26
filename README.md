# Clovis Mobile

> **Note (26/08/2026)** : la migration vers Capacitor dans `clovis-frontend`
> (voir `android/` et `ios/` là-bas) est terminée pour tous les lots
> ci-dessous, **sauf le Lot 1 (temps d'usage par app)** qui n'a pas été
> migré et n'existe donc encore que dans ce dépôt. Ce dépôt garde
> `android-legacy-natif/` et `ios-legacy-natif/` comme référence historique.
> Ne pas relancer de setup Capacitor ici.
>
> Tous les lots migrés ont été **compilés et testés sur appareil réel**.

Bras armé de Clovis sur le téléphone de l'étudiant (voir `00-commun.md` et
`01-socle-app-android.md` du chantier "programme adaptatif étudiant, Partie
3"). Deux projets natifs séparés à l'origine (ce dépôt) :

- `android-legacy-natif/` — Kotlin, Jetpack Compose, deux flavors (`play` et `externe`)
- `ios-legacy-natif/` — Swift, SwiftUI

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
- Correction en construisant le Lot 3 : le dossier `android/app/src/main/res/`
  **n'existait pas du tout**, alors que `AndroidManifest.xml` référençait déjà
  `@mipmap/ic_launcher` — n'aurait pas pu compiler tel quel. Ajouté (icône
  adaptative provisoire, à remplacer par la vraie identité visuelle Clovis).

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
- Niveau d'accès : create/read/rename/delete/move testés sur appareil réel
  sur les deux plateformes.

## État au 23/08/2026 (Lot 3 — notifications & rappels)

- Table Supabase `appareils_mobiles_push_tokens` créée en prod, backend
  `clovis-backend` étendu (`core/notifications_push.py`) : le système de
  rappels existant (déjà utilisé par le navigateur/PWA) livre maintenant
  aussi vers Android (FCM) et iOS (APNs), sans duplication — l'outil IA
  `planifier_rappel` déjà exposé à Clovis n'a pas changé, il atteint
  simplement plus de canaux qu'avant. Nouveaux endpoints
  `POST`/`DELETE /api/appareils-mobiles/push-token`.
- Android : notifications classiques + alerte prioritaire avec repli
  automatique (voir découverte importante ci-dessous), alarme et
  calendrier via intents natifs (délégués à l'app native, jamais
  silencieux), tuile réglages rapides + widget écran d'accueil (option A
  du raccourci "Clovis joignable partout", voir échange avec Bourama),
  onglet "Rappels" ajouté à la barre de navigation existante.
- iOS : APNs, notifications locales + rappel programmé à une date donnée
  (équivalent alarme), calendrier via EventKit (UI native), raccourci
  Siri/Centre de contrôle via App Intents (équivalent iOS de la tuile
  Android, sans target d'extension séparé).
- **Découverte importante, absente du document de cadrage** : depuis une
  politique Google Play appliquée en 2025, l'alerte plein écran Android
  (`USE_FULL_SCREEN_INTENT`) n'est accordée par défaut qu'aux apps
  classées appels/alarme par le Play Store — Clovis n'en fait pas partie,
  la permission sera donc probablement refusée sur le flavor `play` sauf
  activation manuelle par l'étudiant. Repli automatique vers une
  notification heads-up classique implémenté côté code.
- **Découverte importante, absente du document de cadrage** : "ouvrir une
  app à la demande" n'est PAS libre sur Android non plus (contrairement à
  ce que le document laissait entendre face à la limite iOS) — depuis
  Android 11, un package non déclaré dans `<queries>` du manifeste est
  invisible à l'app, même installé. Chaque app tierce à ouvrir doit être
  ajoutée une par une, à confirmer avec Bourama lesquelles. Même limite
  reconnue côté iOS (`LSApplicationQueriesSchemes`, actuellement vide).
- iOS n'a **aucun équivalent** à l'alerte plein écran Android (réservé à
  CallKit/téléphonie) — remplacé par une notification "time-sensitive"
  (traverse Ne pas déranger, mais reste une notification standard).
- Non fait, nécessite Xcode directement : widget d'écran d'accueil complet
  (WidgetKit, target d'extension séparé) — voir `ios/README.md`.

## État au 23/08/2026 (Lot 4 — contrôles de session)

- **Android** : nouvel écran `ControleSessionScreen.kt` + repository
  `ControleSessionRepository.kt`. Bascule DND (`INTERRUPTION_FILTER_ALARMS`,
  à ajuster si Bourama veut un filtre différent) et coupe volume
  sonnerie/notifications, via la permission spéciale "Accès à la Politique de
  Notification" (`ACCESS_NOTIFICATION_POLICY`, ajoutée au manifeste commun).
  État initial capturé avant modification et restauré exactement à l'arrêt.
  Onglet "Session" ajouté à la barre de navigation existante, en premier
  onglet.
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
- Critère de fin (connecteur fonctionnel de bout en bout) : testé sur
  appareil réel, code en place sur les trois dépôts.

## Lot 6 — Service d'accessibilité (23/08/2026)

Flavor `externe` uniquement, comme prévu (`06-service-accessibilite.md`).

- `ServiceAccessibiliteClovis` : lecture seule (aucune action, portée
  stricte du Lot 6), journalise ce qu'il observe.
- Écran de consentement en langage simple, affiché avant l'écran système.
- Isolation garantie au niveau des sources compilées : le flavor `play` ne
  contient aucune ligne de code d'accessibilité (stub vide dans
  `src/play/.../ModuleAccessibilite.kt`), pas juste un interrupteur runtime.
- Testé sur appareil réel, lecture vérifiée sur plusieurs apps tierces.

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
- Testé sur appareil réel : action de bout en bout fiable, repli vérifié
  en cassant volontairement le scénario (app fermée, élément renommé/
  déplacé, app non autorisée).

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

## ✅ État au 26/08/2026 : compilé et testé, migré vers Capacitor

Tous les lots (1 à 8) sont maintenant :
- **Compilés et testés sur appareil réel**, Android (`play` et `externe`)
  et iOS.
- **Migrés en plugins Capacitor dans `clovis-frontend`** (`android/` et
  `ios/` là-bas), à l'exception du **Lot 1 (temps d'usage par app)** qui
  reste uniquement dans ce dépôt legacy, non migré.

Ce dépôt (`android-legacy-natif/`, `ios-legacy-natif/`) n'est plus
maintenu comme app à part entière : il sert de référence historique pour
le code déjà migré, et reste la seule implémentation existante du temps
d'usage par app tant que ce lot n'est pas migré.

## Prochains chantiers

- Migrer le Lot 1 (temps d'usage par app) en plugin Capacitor dans
  `clovis-frontend`, seul lot encore non migré.
- Connecter les capacités mobiles (notamment Lots 6/7, accessibilité et
  actions pilotées) au chat Clovis pour que l'IA puisse décider et
  exécuter des actions sur l'appareil.
