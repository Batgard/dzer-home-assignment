package com.deezer.exoapplication.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deezer.exoapplication.core.data.DummyTracksDataSource
import com.deezer.exoapplication.playlist.data.repository.SimpleDeezerRepository
import com.deezer.exoapplication.core.data.SimpleListeningQueueRepository
import com.deezer.exoapplication.core.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.models.Track
import com.deezer.exoapplication.playlist.domain.usecases.GetTracksWithPreviewUseCase
import com.deezer.exoapplication.playlist.fwk.remote.DeezerApiImpl
import com.deezer.exoapplication.playlist.fwk.utils.AndroidUrlValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val getTracksWithPreview: GetTracksWithPreviewUseCase,
    private val queueRepository: ListeningQueueRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val trackListState: MutableStateFlow<TrackListState> = MutableStateFlow(TrackListState.Initial)
    val state: Flow<UiState> =
        combine(queueRepository.getQueue(), trackListState) { queue, trackList ->
            when (trackList) {
                TrackListState.Initial -> UiState.Loading
                TrackListState.Empty -> UiState.Empty
                is TrackListState.Success -> UiState.Success(trackList.tracks.map { track ->
                    track.toUiModel(queue.firstOrNull { it.id == track.id} != null)
                })

                is TrackListState.Error -> UiState.Error(trackList.message)
            }
        }

    init {
        viewModelScope.launch(dispatcher) {
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
        viewModelScope.launch(dispatcher) {
            val track =
                (trackListState.value as? TrackListState.Success)?.tracks?.find { it.id == trackId }
                    ?: throw IllegalArgumentException("Track not found") // TODO: Just show a toaster with an error message and log the issue to the monitoring tool (Crashlytics or something else)
            if (queueRepository.getQueue().value.contains(track)) {
                queueRepository.removeTrackFromQueue(track.id)
            } else {
                queueRepository.addTrackToQueue(track)
            }
        }
    }

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
                return TrackListViewModel(
                    getTracksWithPreview = GetTracksWithPreviewUseCase(
                        SimpleDeezerRepository(DeezerApiImpl()),
                        urlValidator = AndroidUrlValidator()
                    ),
                    queueRepository = SimpleListeningQueueRepository(DummyTracksDataSource),
                ) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}

