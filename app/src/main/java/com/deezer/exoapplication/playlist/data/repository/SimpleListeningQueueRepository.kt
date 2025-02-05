package com.deezer.exoapplication.playlist.data.repository

import android.content.res.Resources.NotFoundException
import com.deezer.exoapplication.playlist.domain.ListeningQueueRepository
import com.deezer.exoapplication.playlist.domain.Track
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.internal.toImmutableList

class SimpleListeningQueueRepository(private val dataSource: TracksDataSource): ListeningQueueRepository {
    override suspend fun getQueue(): List<Track> { // TODO: Shall we use a flow so that whenever there's a change in the queue, the collectors get notified
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
    suspend fun getTracks(): List<Track>
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track): Boolean
}

object DummyTracksDataSource: TracksDataSource { // TODO: Store IDs in database and files on disk

    private val mutex = Mutex()
    private val tracks: MutableList<Track> = mutableListOf()

    override suspend fun getTracks(): List<Track> {
        val list = mutex.withLock {
            tracks.toImmutableList()
        }
        return list
    }

    override suspend fun addTrack(track: Track) {
        mutex.withLock {
            tracks.add(track)
        }
    }

    override suspend fun removeTrack(track: Track): Boolean {
        return mutex.withLock {
            val trackToRemove = tracks.find { it.id == track.id }
            tracks.remove(trackToRemove)
        }
    }
}