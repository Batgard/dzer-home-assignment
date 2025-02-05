package com.deezer.exoapplication.playlist.domain

interface ListeningQueueRepository {
    suspend fun getQueue(): List<Track>
    suspend fun addTrackToQueue(track: Track): Result<Unit>
    suspend fun removeTrackFromQueue(track: Track): Result<Unit>
}