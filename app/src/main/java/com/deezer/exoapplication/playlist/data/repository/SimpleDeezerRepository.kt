package com.deezer.exoapplication.playlist.data.repository

import com.deezer.exoapplication.playlist.domain.DeezerRepository
import com.deezer.exoapplication.playlist.fwk.models.Track
import com.deezer.exoapplication.playlist.fwk.remote.DeezerApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimpleDeezerRepository(
    private val deezerApi: DeezerApi,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DeezerRepository {
    override suspend fun getTrackList(): Result<List<Track>> {
        return try {
            Result.success(withContext(defaultDispatcher) {
                deezerApi.getTrackList().tracks
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
