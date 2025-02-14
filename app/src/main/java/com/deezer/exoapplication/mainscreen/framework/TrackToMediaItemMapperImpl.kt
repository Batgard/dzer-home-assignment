package com.deezer.exoapplication.mainscreen.framework

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.deezer.exoapplication.mainscreen.presentation.MainScreenViewModel
import com.deezer.exoapplication.mainscreen.presentation.TrackToMediaItemMapper
import com.deezer.exoapplication.playlist.domain.models.Track

class TrackToMediaItemMapperImpl : TrackToMediaItemMapper {
    override fun mapTrackToMediaItem(track: Track): MainScreenViewModel.PlayerMediaItem =
        with(track) {
            MainScreenViewModel.PlayerMediaItem(
                MediaItem.Builder()
                    .setUri(previewUrl)
                    .setMediaId(id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(title)
                            .setArtist(artistName)
                            .setArtworkUri(Uri.parse(coverImageUrl))
                            .build()
                    )
                    .build()
            )
        }
}