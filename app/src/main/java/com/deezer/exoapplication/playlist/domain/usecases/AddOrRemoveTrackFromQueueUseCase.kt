package com.deezer.exoapplication.playlist.domain.usecases

import com.deezer.exoapplication.core.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.models.Track

class AddOrRemoveTrackFromQueueUseCase(private val queueRepository: ListeningQueueRepository) {
    suspend operator fun invoke(track: Track): Result<Unit> = runCatching {
        if (queueRepository.getQueue().value.contains(track)) {
            queueRepository.removeTrackFromQueue(track.id)
        } else {
            queueRepository.addTrackToQueue(track)
        }
    }
}