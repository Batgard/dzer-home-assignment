package com.deezer.exoapplication.playlist.domain.models

data class Track(
    val id: Int,
    val title: String,
    val durationInSeconds: Int,
    val coverImageUrl: String,
    val artistName: String,
    val albumTitle: String,
    val previewUrl: String,
    val readable: Boolean,
)