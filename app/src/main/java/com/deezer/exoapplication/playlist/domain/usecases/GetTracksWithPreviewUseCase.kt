package com.deezer.exoapplication.playlist.domain.usecases

import com.deezer.exoapplication.playlist.domain.DeezerRepository
import com.deezer.exoapplication.playlist.domain.models.Track
import com.deezer.exoapplication.playlist.domain.utils.UrlValidator

class GetTracksWithPreviewUseCase(
    private val deezerRepository: DeezerRepository,
    private val urlValidator: UrlValidator,
) {
    suspend operator fun invoke(): Result<List<Track>> {
        return deezerRepository.getTrackList().onFailure {
            Result.failure<Exception>(it)
        }.onSuccess { tracks ->
            tracks.filter {
                it.readable &&
                urlValidator.isUrlValid(it.previewUrl)
            }
        }
    }
}

