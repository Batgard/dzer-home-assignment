package com.deezer.exoapplication.player.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

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

    private val selectedTrackId: MutableStateFlow<Int> = MutableStateFlow(0)
    private val unplayableTracks: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
    val state: Flow<UiState> = combine(
        queueRepository.getQueue(),
        selectedTrackId,
        unplayableTracks,
    ) { queue, selectedTrackId, unplayableTracks ->
        if (queue.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(
                queue.map { it.copy(readable = unplayableTracks.contains(it.id).not()) },
                getSelectedTrackIndex(queue, selectedTrackId),
                queue.map { it.toMediaItem() },
            )
        }
    }


    private fun getSelectedTrackIndex(queue: List<Track>, trackId: Int): Int {
        val indexOfFirst = queue.indexOfFirst { it.id == trackId }
        Log.d("Player", "getSelectedTrackIndex: ${indexOfFirst}")
        return indexOfFirst.coerceAtLeast(0)
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
                handlePlayerError(playerEvent)
            }

            is PlayerEvent.SelectedTrackChanged -> {
                handleSelectedTrackChange(playerEvent)
            }
        }
    }

    fun onQueueEvent(queueEvent: QueueEvent) {
        when (queueEvent) {
            is QueueEvent.TrackRemovalRequest -> {
                handleTrackRemovalRequest(queueEvent)
            }
            is QueueEvent.TrackSelected -> {
                handleTrackSelected(queueEvent)
            }
        }
    }

    private fun handleTrackRemovalRequest(queueEvent: QueueEvent.TrackRemovalRequest) {
        viewModelScope.launch(coroutineDispatcher) {
            queueRepository.removeTrackFromQueue(queueEvent.trackId)
        }
    }

    private fun handleTrackSelected(queueEvent: QueueEvent.TrackSelected) {
        selectedTrackId.update {
            queueEvent.trackId // FIXME: Double check if that's enough
        }
    }

    private fun handleSelectedTrackChange(playerEvent: PlayerEvent.SelectedTrackChanged) {
        Log.d("Player", "SelectedTrackChanged: ${playerEvent.trackId}")
        selectedTrackId.update {
            playerEvent.trackId.toInt()
        }
    }

    private fun handlePlayerError(playerEvent: PlayerEvent.Error) {
        Log.d("Player", "Error: ${playerEvent.error}")
        val queue = queueRepository.getQueue().value
        val selectedTrackId = selectedTrackId.value
        val indexOfSelectedTrack =
            queue.indexOfFirst { it.id == selectedTrackId }
        if (indexOfSelectedTrack < queue.lastIndex) {
            this.selectedTrackId.update {
                queue[indexOfSelectedTrack + 1].id
            }
        }
        unplayableTracks.update { unplayableTracks ->
            unplayableTracks + selectedTrackId
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
        data class Error(val error: PlaybackException) : PlayerEvent
        data class SelectedTrackChanged(val trackId: String) : PlayerEvent
    }

    sealed interface QueueEvent {
        data class TrackSelected(val trackId: Int) : QueueEvent
        data class TrackRemovalRequest(val trackId: Int) : QueueEvent
    }
}