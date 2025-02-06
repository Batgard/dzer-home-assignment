package com.deezer.exoapplication.playlist.domain

import kotlinx.coroutines.flow.StateFlow

interface ListeningQueueRepository {
    fun getQueue(): StateFlow<List<Track>>
    suspend fun addTrackToQueue(track: Track): Result<Unit>
    suspend fun removeTrackFromQueue(track: Track): Result<Unit>
}
