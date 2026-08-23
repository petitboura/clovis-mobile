# Clovis Mobile — iOS

## Pourquoi il n'y a pas de `.xcodeproj` ici

Un projet Xcode se crée normalement via l'interface graphique Xcode, pas en
écrivant le fichier `.xcodeproj` à la main (fragile, sujet à erreurs). Ce
dossier contient donc le code Swift prêt à l'emploi (`ClovisMobile/Sources/`),
à intégrer dans un projet créé via Xcode.

**Étapes (sur un Mac avec Xcode installé) :**

1. Xcode → File → New → Project → iOS → App.
   - Nom : `ClovisMobile`
   - Interface : SwiftUI
   - Langage : Swift
   - Bundle identifier : à définir avec Bourama (jamais de référence à
     Djiguignè, voir `00-commun.md`) — proposition : `com.clovis.app`
2. Supprimer les fichiers par défaut générés par Xcode (`ContentView.swift`,
   etc.), copier à la place tout le contenu de `ClovisMobile/Sources/` dans
   le projet.
3. Remplacer le `Info.plist` généré par celui fourni ici (`ClovisMobile/Info.plist`),
   ou fusionner les clés si Xcode en a ajouté d'autres.
4. Ajouter la dépendance Swift Package `supabase-swift`
   (`https://github.com/supabase/supabase-swift`) via File → Add Package
   Dependencies.
5. Signing & Capabilities → ajouter `ClovisMobile.entitlements` fourni ici
   (nécessite le compte Apple Developer, voir plus bas).

## Limite réelle vérifiée : temps par app sur iOS

Recherche faite le 23/08/2026 avant d'écrire ce code (voir aussi le
commentaire en tête de `Sources/Screens/UsageScreen.swift`) :

- iOS n'a **aucun équivalent direct** d'`UsageStatsManager` (Android) pour
  une app tierce. Le framework concerné est **Screen Time**
  (`FamilyControls` + `DeviceActivity` + `DeviceActivityReport`).
- Pour ne serait-ce que *demander* l'autorisation `.individual`
  (auto-surveillance, pas parentale), il faut :
  1. Un compte **Apple Developer Program actif** (payant) — Bourama n'en a
     pas encore (confirmé le 23/08).
  2. L'**entitlement `com.apple.developer.family-controls`**, qui se demande
     séparément à Apple via un formulaire dédié ("Family Controls
     Distribution"), et n'est pas garanti automatiquement même avec un
     compte payant.
- **Même une fois obtenu**, Apple ne donne pas les noms d'app ni les durées
  exactes en clair au code de l'app : l'affichage détaillé passe par un
  target d'extension séparé (`DeviceActivityReportExtension`), rendu par
  Apple dans un cadre protégé — l'app hôte ne peut pas lire ces chiffres
  comme de simples données pour les renvoyer à `clovis-backend`.

**Conséquence concrète** : contrairement à Android, la synchronisation du
temps par app vers le backend n'est pas possible avec des chiffres exacts
sur iOS dans l'état actuel, sans solution de contournement fragile. Point à
remonter à Bourama tel quel (fait), plutôt que de forcer quelque chose — voir
`00-commun.md`, règle explicite sur ce point.

**Ce qui EST fait dans ce lot côté iOS** :
- Auth Supabase (identique à Android/web).
- Demande d'autorisation `.individual` (le bouton fonctionne, une fois
  l'entitlement en place).
- Écran qui explique honnêtement la limite à l'étudiant plutôt que de
  prétendre afficher des chiffres qu'on n'a pas.

**Prochaine étape suggérée** : une fois le compte Apple Developer créé et
l'entitlement demandé, ajouter le target `DeviceActivityReportExtension`
dans Xcode et brancher le rendu (commentaire TODO déjà en place dans
`UsageScreen.swift`).

## Lot 3 (notifications & rappels), 23/08/2026

**Étapes Xcode supplémentaires pour ce lot :**

6. Signing & Capabilities → ajouter la capacité **Push Notifications**
   (coche l'entitlement `aps-environment`, déjà présent dans
   `ClovisMobile.entitlements`) — nécessite le compte Apple Developer
   Program (même dépendance que Family Controls, voir plus haut).
7. Les frameworks `EventKit`, `EventKitUI`, `UserNotifications`, `AppIntents`
   sont tous des frameworks système Apple, aucune dépendance externe à
   ajouter pour eux.

**Limite réelle vérifiée : alerte plein écran**

Recherche faite avant de coder ce lot : iOS n'a **aucun équivalent** à
l'alerte plein écran par-dessus le verrouillage qu'Android autorise (même
partiellement) pour une app tierce classique. Cette capacité est réservée
historiquement à CallKit (`PKPushRegistry` + `CXProvider`), un cadre conçu
pour simuler un appel entrant — l'utiliser pour un simple rappel d'étude
serait un détournement que l'App Store review rejette. Remplacé ici par
`UNNotificationInterruptionLevel.timeSensitive` (traverse Ne pas déranger,
mais reste une notification standard, pas un écran plein) — voir
`Sources/Notifications/NotificationsNatives.swift`.

**Ce qui EST fait dans ce lot côté iOS :**
- Enregistrement APNs (`ClovisAppDelegate.swift`) et envoi du token à
  `clovis-backend` (`POST /api/appareils-mobiles/push-token`).
- Notifications locales de test + rappel programmé à une date donnée
  (équivalent iOS d'une alarme, voir `NotificationsNatives.swift`).
- Ajout d'événement au calendrier via l'UI native EventKit
  (`EKEventEditViewController`, l'étudiant confirme avant tout ajout réel).
- Raccourci Siri/Centre de contrôle "Ouvrir Clovis" via App Intents
  (`ClovisAppIntent.swift`) — équivalent de la tuile réglages rapides
  Android, ne nécessite pas de target d'extension séparé.

**Ce qui N'EST PAS fait, à faire dans Xcode directement (pas scriptable
depuis ce dépôt) :**
- Widget d'écran d'accueil complet (WidgetKit) — contrairement à App
  Intents ci-dessus, un widget WidgetKit nécessite un **target
  d'extension séparé** créé depuis Xcode (File → New → Target → Widget
  Extension), même chose que `DeviceActivityReportExtension` plus haut.
  Pas fait dans ce lot, à ajouter si Bourama le demande — le raccourci
  App Intents couvre déjà le besoin minimal de "Clovis accessible en un
  geste".
- Ouverture d'app tierce (`RappelsNatifs.ouvrirApp`) : fonctionne
  uniquement pour un schéma d'URL explicitement ajouté à
  `LSApplicationQueriesSchemes` dans `Info.plist` (actuellement vide,
  aucune app demandée par Bourama) — jamais une liste ouverte comme sur
  Android avant sa propre restriction de visibilité de packages (voir
  `clovis-mobile/android/.../RappelsNatifs.kt` pour le détail équivalent
  côté Android, découvert dans ce même lot).
