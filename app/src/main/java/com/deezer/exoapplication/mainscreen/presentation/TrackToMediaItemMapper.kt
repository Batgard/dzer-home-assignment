package com.deezer.exoapplication.mainscreen.presentation

import com.deezer.exoapplication.playlist.domain.models.Track

interface TrackToMediaItemMapper {
    fun mapTrackToMediaItem(track: Track): MainScreenViewModel.PlayerMediaItem
}