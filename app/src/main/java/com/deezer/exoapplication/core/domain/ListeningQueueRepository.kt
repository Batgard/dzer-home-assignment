package com.deezer.exoapplication.core.domain

import com.deezer.exoapplication.playlist.domain.models.Track
import kotlinx.coroutines.flow.StateFlow

interface ListeningQueueRepository {
    fun getQueue(): StateFlow<List<Track>>
    suspend fun addTrackToQueue(track: Track): Result<Unit>
    suspend fun removeTrackFromQueue(trackId: Int): Result<Unit>
}
