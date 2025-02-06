package com.deezer.exoapplication.playlist.data.repository

import android.content.res.Resources.NotFoundException
import com.deezer.exoapplication.playlist.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SimpleListeningQueueRepository(private val dataSource: TracksDataSource) :
    ListeningQueueRepository {
    override fun getQueue(): StateFlow<List<Track>> {
        return dataSource.getTracks()
    }

    override suspend fun addTrackToQueue(track: Track): Result<Unit> {
        dataSource.addTrack(track)
        return Result.success(Unit)
    }

    override suspend fun removeTrackFromQueue(track: Track): Result<Unit> {
        return if (dataSource.removeTrack(track)) {
            Result.success(Unit)
        } else {
            Result.failure(NotFoundException("Track ${track.title} not found in queue"))
        }
    }
}

interface TracksDataSource {
    fun getTracks(): StateFlow<List<Track>>
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track): Boolean
}

object DummyTracksDataSource : TracksDataSource { // TODO: Store IDs in database and files on disk

    private val tracksFlow: MutableStateFlow<List<Track>> = MutableStateFlow(emptyList())

    override fun getTracks(): StateFlow<List<Track>> {
        return tracksFlow.asStateFlow()
    }

    override suspend fun addTrack(track: Track) {
        tracksFlow.update {
            it + track
        }
    }

    override suspend fun removeTrack(track: Track): Boolean {
        tracksFlow.update {
            it - track
        }
        return true
    }
}