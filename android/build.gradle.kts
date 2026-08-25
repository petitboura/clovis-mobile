// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle Android.
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
    // Ajoute le 25/08/2026, Bourama : necessaire pour que les classes
    // @Serializable (ReponseActionsEnAttente, etc.) generent vraiment leur
    // serializer a la compilation -- sans lui, l'annotation ne fait rien et
    // la deserialisation des reponses serveur echoue avec
    // "Serializer for class '...' is not found" (voir diagnostic du 25/08).
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0" apply false
}
