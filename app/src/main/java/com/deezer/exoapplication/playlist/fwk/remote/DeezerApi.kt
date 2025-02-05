package com.deezer.exoapplication.playlist.fwk.remote

import com.deezer.exoapplication.playlist.fwk.models.TracksResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface DeezerApi {
    suspend fun getTrackList(): TracksResponse
}

class DeezerApiImpl : DeezerApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun getTrackList(): TracksResponse {
        return client.get("https://api.deezer.com/playlist/160504851/tracks").body()
    }
}
