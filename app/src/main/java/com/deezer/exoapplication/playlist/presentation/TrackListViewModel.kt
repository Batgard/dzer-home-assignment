package com.deezer.exoapplication.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deezer.exoapplication.playlist.data.repository.SimpleDeezerRepository
import com.deezer.exoapplication.playlist.domain.DeezerRepository
import com.deezer.exoapplication.playlist.domain.Track
import com.deezer.exoapplication.playlist.fwk.remote.DeezerApiImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val repository: DeezerRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            repository.getTrackList().onSuccess { tracks ->
                _state.update {
                    if (tracks.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(tracks)
                    }
                }
            }.onFailure { exception ->
                _state.update {
                    UiState.Error(exception.message ?: "Unknown error")
                }
            }
        }
    }

    fun onTrackClick(it: Int) {
        // TODO: Add track to queue
    }

    sealed interface UiState {
        object Loading : UiState
        object Empty : UiState
        data class Success(val tracks: List<Track>) : UiState
        data class Error(val message: String) : UiState
    }

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackListViewModel::class.java)) {
                return TrackListViewModel(
                    repository = SimpleDeezerRepository(DeezerApiImpl()),
                ) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
    }
}

