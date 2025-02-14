package com.deezer.exoapplication.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deezer.exoapplication.core.data.DummyTracksDataSource
import com.deezer.exoapplication.core.data.SimpleListeningQueueRepository
import com.deezer.exoapplication.core.domain.GetListeningQueueUseCase
import com.deezer.exoapplication.playlist.data.repository.SimpleDeezerRepository
import com.deezer.exoapplication.playlist.domain.models.Track
import com.deezer.exoapplication.playlist.domain.usecases.AddOrRemoveTrackFromQueueUseCase
import com.deezer.exoapplication.playlist.domain.usecases.GetTracksWithPreviewUseCase
import com.deezer.exoapplication.playlist.framework.remote.DeezerApiImpl
import com.deezer.exoapplication.playlist.framework.utils.AndroidUrlValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val getTracksWithPreview: GetTracksWithPreviewUseCase,
    private val addOrRemoveTrackFromQueue: AddOrRemoveTrackFromQueueUseCase,
    private val getListeningQueue: GetListeningQueueUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
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
    private val subcoroutineContext = dispatcher + exceptionHandler

    private val trackListState: MutableStateFlow<TrackListState> =
        MutableStateFlow(TrackListState.Initial)
    val state: Flow<UiState> =
        combine(getListeningQueue(), trackListState) { queue, trackList ->
            when (trackList) {
                TrackListState.Initial -> UiState.Loading
                TrackListState.Empty -> UiState.Empty
                is TrackListState.Success -> UiState.Success(trackList.tracks.map { track ->
                    track.toUiModel(queue.firstOrNull { it.id == track.id } != null)
                })

                is TrackListState.Error -> UiState.Error(trackList.message)
            }
        }

    init {
        viewModelScope.launch(subcoroutineContext) {
            getTracksWithPreview().onSuccess { tracks ->
                trackListState.update {
                    TrackListState.Success(tracks)
                }
            }.onFailure { exception ->
                trackListState.update {
                    TrackListState.Error(exception.message ?: "Unknown error")
                }
            }
        }
    }

    fun onTrackClick(trackId: Int) {
        viewModelScope.launch(subcoroutineContext) {
            val track = findTrackInState(trackId)
            addOrRemoveTrackFromQueue(track).onFailure {
                // TODO: Show error message, not blocking and report the issue
            }
        }
    }

    private fun findTrackInState(trackId: Int) =
        ((trackListState.value as? TrackListState.Success)?.tracks?.find { it.id == trackId }
            ?: throw IllegalArgumentException("Track not found"))  // TODO: Just show a toaster with an error message and log the issue to the monitoring tool (Crashlytics or something else)

    private fun Track.toUiModel(isQueued: Boolean): TrackUiModel {
        return TrackUiModel(
            id = id,
            title = title,
            durationInSeconds = durationInSeconds,
            coverImageUrl = coverImageUrl,
            artistName = artistName,
            albumTitle = albumTitle,
            isInQueue = isQueued,
        )
    }

    sealed interface TrackListState {
        object Initial : TrackListState
        object Empty : TrackListState
        data class Success(val tracks: List<Track>) : TrackListState
        data class Error(val message: String) : TrackListState
    }

    sealed interface UiState {
        object Loading : UiState
        object Empty : UiState
        data class Success(val tracks: List<TrackUiModel>) : UiState
        data class Error(val message: String) : UiState
    }

    data class TrackUiModel(
        val id: Int,
        val title: String,
        val durationInSeconds: Int,
        val coverImageUrl: String,
        val artistName: String,
        val albumTitle: String,
        val isInQueue: Boolean = false,
    )

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackListViewModel::class.java)) {
                val queueRepository = SimpleListeningQueueRepository(DummyTracksDataSource)
                return TrackListViewModel(
                    getTracksWithPreview = GetTracksWithPreviewUseCase(
                        SimpleDeezerRepository(DeezerApiImpl()),
                        urlValidator = AndroidUrlValidator()
                    ),
                    getListeningQueue = GetListeningQueueUseCase(queueRepository),
                    addOrRemoveTrackFromQueue = AddOrRemoveTrackFromQueueUseCase(queueRepository)
                ) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}

