package com.deezer.exoapplication.playlist.domain

import com.deezer.exoapplication.playlist.domain.models.Track

interface DeezerRepository {
    suspend fun getTrackList(): Result<List<Track>>
}
