package com.deezer.exoapplication.core.domain

import com.deezer.exoapplication.playlist.domain.models.Track
import kotlinx.coroutines.flow.StateFlow

class GetListeningQueueUseCase(private val repository: ListeningQueueRepository) {
    operator fun invoke(): StateFlow<List<Track>> = repository.getQueue()
}