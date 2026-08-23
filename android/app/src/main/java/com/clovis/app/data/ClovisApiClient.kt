// Cree le 23/08/2026, Bourama : Lot 1 Partie 3 (app mobile), socle.
//
// Canal dedie entre l'app mobile et clovis-backend, voir
// clovis-backend/api/appareils_mobiles.py. Meme auth Bearer que
// clovis-frontend (access_token Supabase), rien de specifique invente ici.
//
// TODO Bourama : remplacer BASE_URL par l'URL Railway reelle de
// clovis-backend en production (et prevoir une variante debug pointant
// vers localhost/reseau local pour les tests, pas encore fait ici).
package com.clovis.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

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
}
