package com.deezer.exoapplication.playlist.framework.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracksResponse(
    @SerialName("data") val tracks: List<Track>,
    @SerialName("checksum") val checksum: String,
    @SerialName("total") val total: Int,
    @SerialName("next") val next: String? = null
)

@Serializable
data class Track(
    @SerialName("id") val id: Int,
    @SerialName("readable") val readable: Boolean,
    @SerialName("title") val title: String,
    @SerialName("title_short") val titleShort: String,
    @SerialName("title_version") val titleVersion: String? = null,
    @SerialName("link") val link: String,
    @SerialName("duration") val duration: Int,
    @SerialName("rank") val rank: Int,
    @SerialName("explicit_lyrics") val explicitLyrics: Boolean,
    @SerialName("explicit_content_lyrics") val explicitContentLyrics: Int,
    @SerialName("explicit_content_cover") val explicitContentCover: Int,
    @SerialName("preview") val preview: String,
    @SerialName("artist") val artist: Artist,
    @SerialName("album") val album: Album,
    @SerialName("type") val type: String
)

@Serializable
data class Artist(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("tracklist") val tracklist: String,
    @SerialName("type") val type: String
)

@Serializable
data class Album(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("cover") val cover: String,
    @SerialName("cover_small") val coverSmall: String,
    @SerialName("cover_medium") val coverMedium: String,
    @SerialName("cover_big") val coverBig: String,
    @SerialName("cover_xl") val coverXl: String,
    @SerialName("tracklist") val trackList: String,
    @SerialName("type") val type: String
)