package com.deezer.exoapplication.mainscreen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import com.deezer.exoapplication.core.data.DummyTracksDataSource
import com.deezer.exoapplication.core.data.SimpleListeningQueueRepository
import com.deezer.exoapplication.core.domain.ListeningQueueRepository
import com.deezer.exoapplication.mainscreen.framework.TrackToMediaItemMapperImpl
import com.deezer.exoapplication.mainscreen.presentation.MainScreenViewModel.PlayerEvent
import com.deezer.exoapplication.mainscreen.presentation.MainScreenViewModel.QueueEvent
import com.deezer.exoapplication.mainscreen.presentation.MainScreenViewModel.UiState
import com.deezer.exoapplication.playlist.domain.models.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Outputs: Provides a [UiState] as a [Flow]
 * Inputs: Either a [PlayerEvent] or a [QueueEvent]
 */
class MainScreenViewModel(
    private val queueRepository: ListeningQueueRepository,
    private val trackToMediaItemMapper: TrackToMediaItemMapper,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        /* TODO: Depending on the context, we might either want to show a generic error message
         or just reset the screen to start afresh (meaning: cleaning the queue and maybe resetting the database
         */
        if (throwable is CancellationException) {
            // Maybe thrown because the screen is no longer needed so don't try to recover from it
        } else {
            // Display error message
            // Log it (locally and/or remotely)
            // reset the viewModel's state
            // Note: this shouldn't happen as all exceptions are channeled through the Result.failure callback
        }
    }

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
            if (noSelectedTrack(selectedTrackId)) {
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

    fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.Error -> {
                handlePlayerError(playerEvent)
            }

            is PlayerEvent.SelectedTrackEnded -> {
                handleSelectedTrackEnd()
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
        viewModelScope.launch(coroutineDispatcher + exceptionHandler) {
            selectedTrackId.emit(
                getNextTrackId(
                    queueRepository.getQueue().value,
                    queueEvent.trackId
                )
            )
            queueRepository.removeTrackFromQueue(queueEvent.trackId)
        }
    }

    private fun handleTrackSelected(queueEvent: QueueEvent.TrackSelected) {
        selectedTrackId.update {
            queueEvent.trackId
        }
    }

    private fun handleSelectedTrackEnd() {
        selectedTrackId.update {
            getNextTrackId(queueRepository.getQueue().value, it)
        }
    }

    private fun noSelectedTrack(selectedTrackId: Int) = selectedTrackId == NO_TRACK_SELECTED

    private fun getSelectedTrackIndex(queue: List<Track>, trackId: Int): Int {
        val indexOfFirst = queue.indexOfFirst { it.id == trackId }
        return indexOfFirst.coerceAtLeast(DEFAULT_SELECTED_TRACK_INDEX)
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
        //TODO: Do something with the playerEvent like logging it to crashlytics or any monitoring tool
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
            val playingMediaItem: PlayerMediaItem,
        ) : UiState
    }

    data class PlayerMediaItem(val mediaItem: MediaItem) {
        override fun equals(other: Any?): Boolean {
            return mediaItem.mediaId == (other as? PlayerMediaItem)?.mediaItem?.mediaId
        }

        override fun hashCode(): Int {
            return mediaItem.mediaId.hashCode()
        }
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
