package com.deezer.exoapplication.playlist.domain

import com.deezer.exoapplication.playlist.fwk.models.Track

interface DeezerRepository {
    suspend fun getTrackList(): Result<List<Track>> //TODO: Map the entity to something relevant to the business logic layer
}
