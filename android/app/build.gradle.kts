// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle Android.
// Voir 00-commun.md : deux flavors, "play" (publiable Play Store, lots 1-5)
// et "externe" (ajoute l'accessibilite des lots 6-8, jamais soumis au Play
// Store). Chaque flavor a son propre AndroidManifest.xml des ce lot, meme si
// les lots 6-8 ne sont pas encore construits, pour garantir l'etancheite
// structurellement (voir 00-commun.md, "isolation au niveau du build, pas
// d'un simple interrupteur en code").
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Lot 8 : signature du flavor "externe", lue depuis un fichier LOCAL jamais
// committe (voir README-SIGNATURE.md). Absent -> pas de signingConfig
// applique, le build "play" (signe par Google Play App Signing) n'en a de
// toute facon pas besoin.
val fichierKeystore = rootProject.file("keystore.properties")
val proprietesKeystore = Properties().apply {
    if (fichierKeystore.exists()) {
        fichierKeystore.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.clovis.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.clovis.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (fichierKeystore.exists()) {
            create("externe") {
                storeFile = file(proprietesKeystore.getProperty("storeFile"))
                storePassword = proprietesKeystore.getProperty("storePassword")
                keyAlias = proprietesKeystore.getProperty("keyAlias")
                keyPassword = proprietesKeystore.getProperty("keyPassword")
            }
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            // Aucun applicationIdSuffix : c'est l'id publie sur le Play Store.
            // Signature geree automatiquement par Google Play App Signing,
            // rien a configurer ici.
        }
        create("externe") {
            dimension = "distribution"
            // Suffixe distinct : jamais le meme id que la version Play Store,
            // pour eviter tout conflit d'installation cote-a-cote et bien
            // marquer qu'il s'agit d'une distribution differente (lots 6-8).
            applicationIdSuffix = ".externe"
            versionNameSuffix = "-externe"
            // Lot 8 : signature manuelle, TOUJOURS la meme cle d'une version
            // a l'autre (voir README-SIGNATURE.md), sinon aucune mise a jour
            // ne pourra s'installer par-dessus l'app existante.
            if (fichierKeystore.exists()) {
                signingConfig = signingConfigs.getByName("externe")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    // Icones (Timer, BarChart, Folder, CreateNewFolder, NoteAdd,
    // InsertDriveFile, RemoveRedEye...) absentes du set de base Compose,
    // necessaires pour plusieurs ecrans (lots 2, 4, 6) -- une seule
    // declaration suffit, retire le doublon introduit par une session parallele.
    implementation("androidx.compose.material:material-icons-extended")

    // Client HTTP pour appeler clovis-backend (POST/GET /api/appareils-mobiles/usage)
    // ET moteur Ktor utilise par supabase-kt (une seule declaration necessaire,
    // les deux s'appuient sur le meme client). Version alignee sur ce que
    // supabase-kt 3.6.0 recommande (Ktor 3.4.x) -- l'ancienne version 2.3.12
    // etait celle d'avant la correction du bug de version supabase-kt
    // ci-dessous, et n'est pas compatible avec supabase-kt 3.x.
    implementation("io.ktor:ktor-client-android:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

    // Auth Supabase native (meme systeme que clovis-frontend, voir core/auth.py cote backend)
    //
    // BUG CORRIGE 23/08/2026 (session compilation Bourama) : la version etait
    // epinglee a 2.6.1, une version PRE-3.0.0 de supabase-kt ou le module
    // s'appelait encore "gotrue-kt". Le code Kotlin (SupabaseAuthClient.kt)
    // utilise deja les imports post-3.0.0 (io.github.jan.supabase.auth.Auth),
    // qui correspondent au module "auth-kt" -- d'ou "Failed to resolve" a la
    // synchronisation Gradle : la version demandait un module qui n'existait
    // pas encore a la version 2.6.1. Passe a 3.6.0 (derniere version stable
    // au 23/08/2026, verifiee sur github.com/supabase-community/supabase-kt/releases).
    implementation(platform("io.github.jan-tennert.supabase:bom:3.6.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")

    // Lot 2 : DocumentFile, wrapper autour de Storage Access Framework (SAF)
    // pour naviguer/creer/renommer/supprimer/deplacer dans un dossier designe.
    implementation("androidx.documentfile:documentfile:1.0.1")
    // Lot 5 : Custom Tabs pour l'OAuth des connecteurs tiers (Notion...),
    // equivalent Android de ASWebAuthenticationSession cote iOS.
    implementation("androidx.browser:browser:1.8.0")

    // Notifications push natives (Lot 3, 23/08/2026) -- initialisees
    // manuellement via FirebaseOptions (voir ClovisFirebaseApp.kt), donc
    // seule la dependance firebase-messaging suffit, pas besoin du BOM
    // Firebase complet ni du plugin google-services.
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
