// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle Android.
// Voir 00-commun.md : deux flavors, "play" (publiable Play Store, lots 1-5)
// et "externe" (ajoute l'accessibilite des lots 6-8, jamais soumis au Play
// Store). Chaque flavor a son propre AndroidManifest.xml des ce lot, meme si
// les lots 6-8 ne sont pas encore construits, pour garantir l'etancheite
// structurellement (voir 00-commun.md, "isolation au niveau du build, pas
// d'un simple interrupteur en code").
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.clovis.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clovis.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            // Aucun applicationIdSuffix : c'est l'id publie sur le Play Store.
        }
        create("externe") {
            dimension = "distribution"
            // Suffixe distinct : jamais le meme id que la version Play Store,
            // pour eviter tout conflit d'installation cote-a-cote et bien
            // marquer qu'il s'agit d'une distribution differente (lots 6-8).
            applicationIdSuffix = ".externe"
            versionNameSuffix = "-externe"
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")

    // Client HTTP pour appeler clovis-backend (POST/GET /api/appareils-mobiles/usage)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    // Auth Supabase native (meme systeme que clovis-frontend, voir core/auth.py cote backend)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.6.1"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-android:2.3.12")

    // Lot 2 : DocumentFile, wrapper autour de Storage Access Framework (SAF)
    // pour naviguer/creer/renommer/supprimer/deplacer dans un dossier designe.
    implementation("androidx.documentfile:documentfile:1.0.1")
    // Icones Folder/CreateNewFolder/NoteAdd/InsertDriveFile absentes du set
    // "core" de Compose -- necessaires pour DossiersScreen.kt (Lot 2).
    implementation("androidx.compose.material:material-icons-extended")
}
