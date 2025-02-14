package com.deezer.exoapplication.mainscreen.domain

import com.deezer.exoapplication.core.domain.ListeningQueueRepository

class RemoveTrackFromQueueUseCase(private val repository: ListeningQueueRepository) {
    suspend operator fun invoke(trackId: Int) {
        repository.removeTrackFromQueue(trackId)
    }
}
