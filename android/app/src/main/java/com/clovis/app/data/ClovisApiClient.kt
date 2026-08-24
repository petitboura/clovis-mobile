// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
//
// Canal dedie entre l'app mobile et clovis-backend, voir
// clovis-backend/api/appareils_mobiles.py. Meme auth Bearer que
// clovis-frontend (access_token Supabase), rien de specifique invente ici.
package com.clovis.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

private const val BASE_URL = "https://clovis-backend-production.up.railway.app"

@Serializable
data class EntreeUsage(
    val nom_app: String,
    val date: String,
    val duree_secondes: Int
)

@Serializable
data class SynchronisationUsage(
    val plateforme: String,
    val entrees: List<EntreeUsage>
)

@Serializable
data class LigneUsage(
    val plateforme: String,
    val nom_app: String,
    val date: String,
    val duree_secondes: Int
)

@Serializable
data class ReponseUsage(val usage: List<LigneUsage>)

// --- Lot 5 : connecteurs tiers (Notion) ---

@Serializable
data class UrlAutorisationNotion(val url_autorisation: String)

@Serializable
data class FinalisationNotion(val code: String, val state: String)

@Serializable
data class ReponseFinalisationNotion(val connecte: Boolean, val espace: String? = null)

@Serializable
data class StatutNotion(val connecte: Boolean)

@Serializable
data class ResultatNotion(val id: String, val type: String, val url: String? = null)

@Serializable
data class ReponseRechercheNotion(val resultats: List<ResultatNotion>)

// --- Lot 3 : notifications push natives ---

@Serializable
data class TokenPush(val plateforme: String, val token: String)

// --- Lot 1A : canal de decision generique (brancher le cerveau) ---
// Voir clovis-backend/core/actions_appareil_mobile.py : aucun type_action
// "officiel" n'existe encore cote agent, ce canal est le pont generique
// lecture/rapport, pas encore branche a une capacite reelle du telephone.

// `parametres` en JsonElement brut (pas de forme fixe supposee) : le
// contenu depend du type_action, qui n'a pas encore ete decide -- voir
// note en tete de core/actions_appareil_mobile.py cote backend.
@Serializable
data class ActionAppareil(
    val id: String,
    val type_action: String,
    val parametres: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class ReponseActionsEnAttente(val actions: List<ActionAppareil>)

@Serializable
data class ResultatAction(val succes: Boolean, val resultat: String = "")

object ClovisApiClient {

    private val http = HttpClient(Android) {
        install(ContentNegotiation) { json() }
    }

    private suspend fun avecAuth(builder: io.ktor.client.request.HttpRequestBuilder) {
        val token = SupabaseAuthClient.accessTokenCourant()
            ?: throw IllegalStateException("Pas de session Supabase active.")
        builder.header("Authorization", "Bearer $token")
    }

    suspend fun synchroniserUsage(payload: SynchronisationUsage): HttpResponse {
        return http.post("$BASE_URL/api/appareils-mobiles/usage") {
            avecAuth(this)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun obtenirUsage(jours: Int = 7): ReponseUsage {
        val reponse: HttpResponse = http.get("$BASE_URL/api/appareils-mobiles/usage?jours=$jours") {
            avecAuth(this)
        }
        return reponse.body()
    }

    suspend fun demarrerConnexionNotion(): UrlAutorisationNotion {
        val reponse: HttpResponse = http.post("$BASE_URL/api/appareils-mobiles/connecteurs/notion/demarrer") {
            avecAuth(this)
        }
        return reponse.body()
    }

    suspend fun finaliserConnexionNotion(code: String, state: String): ReponseFinalisationNotion {
        val reponse: HttpResponse = http.post("$BASE_URL/api/appareils-mobiles/connecteurs/notion/finaliser") {
            avecAuth(this)
            contentType(ContentType.Application.Json)
            setBody(FinalisationNotion(code, state))
        }
        return reponse.body()
    }

    suspend fun statutNotion(): StatutNotion {
        val reponse: HttpResponse = http.get("$BASE_URL/api/appareils-mobiles/connecteurs/notion/statut") {
            avecAuth(this)
        }
        return reponse.body()
    }

    suspend fun rechercherNotion(requete: String): ReponseRechercheNotion {
        val reponse: HttpResponse = http.get("$BASE_URL/api/appareils-mobiles/connecteurs/notion/rechercher?q=$requete") {
            avecAuth(this)
        }
        return reponse.body()
    }

    // Ajoute le 23/08/2026, Lot 3 (notifications & rappels) : enregistre le
    // token FCM aupres de clovis-backend, voir onNewToken dans
    // ClovisFirebaseMessagingService.kt (appele a chaque obtention/
    // renouvellement du token, pas seulement au premier lancement).
    suspend fun enregistrerPushToken(payload: TokenPush): HttpResponse {
        return http.post("$BASE_URL/api/appareils-mobiles/push-token") {
            avecAuth(this)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun desinscrirePushToken(token: String): HttpResponse {
        return http.delete("$BASE_URL/api/appareils-mobiles/push-token?token=$token") {
            avecAuth(this)
        }
    }

    // --- Lot 1A : canal de decision generique ---

    suspend fun obtenirActionsEnAttente(): ReponseActionsEnAttente {
        val reponse: HttpResponse = http.get("$BASE_URL/api/appareils-mobiles/actions/en-attente") {
            avecAuth(this)
        }
        return reponse.body()
    }

    suspend fun obtenirAction(actionId: String): ActionAppareil {
        val reponse: HttpResponse = http.get("$BASE_URL/api/appareils-mobiles/actions/$actionId") {
            avecAuth(this)
        }
        return reponse.body()
    }

    suspend fun rapporterResultatAction(actionId: String, resultat: ResultatAction): HttpResponse {
        return http.post("$BASE_URL/api/appareils-mobiles/actions/$actionId/resultat") {
            avecAuth(this)
            contentType(ContentType.Application.Json)
            setBody(resultat)
        }
    }
}
