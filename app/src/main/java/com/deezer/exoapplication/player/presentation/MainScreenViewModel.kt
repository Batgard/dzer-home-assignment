package com.deezer.exoapplication.player.presentation

import android.net.Uri
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
class MainScreenViewModel(
    private val queueRepository: ListeningQueueRepository,
    private val trackToMediaItemMapper: TrackToMediaItemMapper,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val selectedTrackId: MutableStateFlow<Int> = MutableStateFlow(NO_TRACK_SELECTED)
    private val unplayableTracks: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
    val state: Flow<UiState> = combine(
        queueRepository.getQueue(),
        selectedTrackId,
        unplayableTracks,
    ) { queue, selectedTrackId, unplayableTracks ->
        if (queue.isEmpty()) {
            UiState.Empty
        } else {
            if (isThereASelectedTrack(selectedTrackId)) {
                this.selectedTrackId.update {
                    queue.first().id
                }
            }
            val currentTrackIndex = getSelectedTrackIndex(queue, selectedTrackId)
            UiState.Success(
                queue.map { it.copy(readable = unplayableTracks.contains(it.id).not()) },
                currentTrackIndex,
                trackToMediaItemMapper.mapTrackToMediaItem(queue[currentTrackIndex]),
            )
        }
    }

    private fun isThereASelectedTrack(selectedTrackId: Int) = selectedTrackId == NO_TRACK_SELECTED


    private fun getSelectedTrackIndex(queue: List<Track>, trackId: Int): Int {
        val indexOfFirst = queue.indexOfFirst { it.id == trackId }
        return indexOfFirst.coerceAtLeast(DEFAULT_SELECTED_TRACK_INDEX)
    }

    fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.Error -> {
                handlePlayerError(playerEvent)
            }

            is PlayerEvent.SelectedTrackEnded -> {
                handleSelectedTrackEnd(playerEvent)
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
            queueEvent.trackId
        }
    }

    private fun handleSelectedTrackEnd(playerEvent: PlayerEvent.SelectedTrackEnded) {
        selectedTrackId.update {
            getNextTrackId(queueRepository.getQueue().value, it)
        }
    }

    private fun getNextTrackId(queue: List<Track>, currentTrackId: Int): Int {
        val indexOfSelectedTrack = getSelectedTrackIndex(queue, currentTrackId)
        return if (indexOfSelectedTrack < queue.lastIndex) {
            queue[indexOfSelectedTrack + 1].id
        } else {
            NO_TRACK_SELECTED
        }
    }

    private fun handlePlayerError(playerEvent: PlayerEvent.Error) {
        //TODO: Do something with the playerEvent
        val queue = queueRepository.getQueue().value
        val selectedTrackId = selectedTrackId.value
        val indexOfSelectedTrack = getSelectedTrackIndex(queue, selectedTrackId)
        if (indexOfSelectedTrack < queue.lastIndex) {
            this.selectedTrackId.update {
                queue[indexOfSelectedTrack + 1].id
            }
        }
        unplayableTracks.update { unplayableTracks ->
            unplayableTracks + selectedTrackId
        }
    }

    companion object {
        const val NO_TRACK_SELECTED = -1
        const val DEFAULT_SELECTED_TRACK_INDEX = 0
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
                return MainScreenViewModel(
                    queueRepository = SimpleListeningQueueRepository(DummyTracksDataSource),
                    trackToMediaItemMapper = TrackToMediaItemMapperImpl(),
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
            val currentTrackIndex: Int = DEFAULT_SELECTED_TRACK_INDEX,
            val playingMediaItem: MediaItem
        ) : UiState
    }

    sealed interface PlayerEvent {
        object SelectedTrackEnded : PlayerEvent
        data class Error(val error: PlaybackException) : PlayerEvent
    }

    sealed interface QueueEvent {
        data class TrackSelected(val trackId: Int) : QueueEvent
        data class TrackRemovalRequest(val trackId: Int) : QueueEvent
    }
}


interface TrackToMediaItemMapper {
    fun mapTrackToMediaItem(track: Track): MediaItem
}

class TrackToMediaItemMapperImpl : TrackToMediaItemMapper {
    override fun mapTrackToMediaItem(track: Track): MediaItem = with(track) {
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

    }
}