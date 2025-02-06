package com.deezer.exoapplication.player.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import com.deezer.exoapplication.playlist.data.repository.DummyTracksDataSource
import com.deezer.exoapplication.playlist.data.repository.SimpleListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * What it should do?
 * Provide player with the url of the track to play
 * let it know if there are tracks left to play
 * Provide player with the next track URL to play when the current one ends (maybe even buffer it already)
 * Provide it with the rest of the information (album cover, artist name, track title)?
 */
class PlayerViewModel(
    private val queueRepository: ListeningQueueRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val selectedTrack: MutableStateFlow<Int> = MutableStateFlow(0)
    val state: Flow<UiState> = combine(
        queueRepository.getQueue(),
        selectedTrack,
    ) { queue, selectedTrackId ->
        if (queue.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(
                queue,
                getSelectedTrackIndex(queue, selectedTrackId),
                queue.map { it.toMediaItem() },
            )
        }
    }


    private fun getSelectedTrackIndex(queue: List<Track>, trackId: Int): Int {
        return queue.indexOfFirst { it.id == trackId }.coerceAtLeast(0)
    }


    private fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
        .setUri(previewUrl)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistName)
                .setArtworkUri(Uri.parse(coverImageUrl)) // Album art
                .build()
        )
        .build()

    fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.Error -> {
            // TODO: Mark track as not readable and move to the next
            }
            is PlayerEvent.SelectedTrackChanged -> {
                selectedTrack.update {
                    playerEvent.trackId.toInt()
                }
            }
        }
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(
                    queueRepository = SimpleListeningQueueRepository(DummyTracksDataSource),
                ) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }

    sealed interface UiState {
        object Empty : UiState
        data class Success(
            val tracks: List<Track>,
            val currentTrackIndex: Int = 0,
            val mediaItems: List<MediaItem>
        ) : UiState
    }

    sealed interface PlayerEvent {
        data class Error(private val error: PlaybackException) : PlayerEvent
        data class SelectedTrackChanged(val trackId: String) : PlayerEvent
    }
}