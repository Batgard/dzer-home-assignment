package com.deezer.exoapplication.playlist.domain.usecases

import android.webkit.URLUtil
import com.deezer.exoapplication.playlist.domain.DeezerRepository
import com.deezer.exoapplication.playlist.domain.Track

class GetTracksWithPreviewUseCase(
    private val deezerRepository: DeezerRepository,
    private val urlValidator: UrlValidator,
) {
    suspend operator fun invoke(): Result<List<Track>> {
        return deezerRepository.getTrackList().onFailure {

        }.onSuccess { tracks ->
            tracks.filter { urlValidator.isUrlValid(it.previewUrl) }
        }
    }
}

interface UrlValidator {
    fun isUrlValid(url: String): Boolean
}

class AndroidUrlValidator() : UrlValidator {
    override fun isUrlValid(url: String) = URLUtil.isValidUrl(url)
}