package com.deezer.exoapplication.playlist.domain

interface DeezerRepository {
    suspend fun getTrackList(): Result<List<Track>>
}

data class Track(
    val id: Int,
    val title: String,
    val durationInSeconds: Int,
    val coverImageUrl: String,
    val artistName: String,
    val albumTitle: String,
    val previewUrl: String,
)
