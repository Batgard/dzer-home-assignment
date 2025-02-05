package com.deezer.exoapplication.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deezer.exoapplication.playlist.data.repository.DummyTracksDataSource
import com.deezer.exoapplication.playlist.data.repository.SimpleDeezerRepository
import com.deezer.exoapplication.playlist.data.repository.SimpleListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.DeezerRepository
import com.deezer.exoapplication.playlist.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.Track
import com.deezer.exoapplication.playlist.fwk.remote.DeezerApiImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val deezerRepository: DeezerRepository,
    private val queueRepository: ListeningQueueRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val queueState: MutableStateFlow<List<Track>> = MutableStateFlow(emptyList())
    private val trackListState: MutableStateFlow<TrackListState> =
        MutableStateFlow(TrackListState.Success())
    val state: Flow<UiState> = combine(queueState, trackListState) { queue, trackList ->
        when (trackList) {
            TrackListState.Empty -> UiState.Empty
            is TrackListState.Success -> UiState.Success(trackList.tracks.map { track ->
                track.toUiModel(queue.contains(track))
            })

            is TrackListState.Error -> UiState.Error(trackList.message)
        }
    }

    init {
        viewModelScope.launch(dispatcher) {
            queueState.update { queueRepository.getQueue() }

            deezerRepository.getTrackList().onSuccess { tracks ->
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
            if (queueState.value.contains(track)) {
                queueRepository.removeTrackFromQueue(track)
            } else {
                queueRepository.addTrackToQueue(track)
            }
            queueState.update { queueRepository.getQueue() } // FIXME: Of course, if repo would return a flow, I wouldn't have to do that
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
        object Empty : TrackListState
        data class Success(val tracks: List<Track> = emptyList()) : TrackListState
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
                    deezerRepository = SimpleDeezerRepository(DeezerApiImpl()),
                    queueRepository = SimpleListeningQueueRepository(DummyTracksDataSource),
                ) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}

