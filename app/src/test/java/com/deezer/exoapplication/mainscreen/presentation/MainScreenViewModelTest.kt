package com.deezer.exoapplication.mainscreen.presentation

import androidx.media3.common.PlaybackException
import app.cash.turbine.test
import com.deezer.exoapplication.core.data.DummyTracksDataSource
import com.deezer.exoapplication.core.data.SimpleListeningQueueRepository
import com.deezer.exoapplication.core.domain.GetListeningQueueUseCase
import com.deezer.exoapplication.mainscreen.domain.RemoveTrackFromQueueUseCase
import com.deezer.exoapplication.playlist.domain.models.Track
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MainScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val trackToMediaItemMapper: TrackToMediaItemMapper = mockk()

    @AfterEach
    fun reset() {
        runBlocking {
            DummyTracksDataSource.removeTrack(track1.id)
            DummyTracksDataSource.removeTrack(track2.id)
        }
    }

    @Test
    fun `Given queue is empty, when state is collected, then UiState is Empty`() = runTest {
        // Given
        val viewModel = createMainScreenViewModel(trackToMediaItemMapper)

        // When
        viewModel.state.test {
            // Then
            val expectedState = MainScreenViewModel.UiState.Empty
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `Given queue is empty, when a track gets added then a new UiState Success is emitted containing that track`() =
        runTest {
            // Given
            val viewModel = createMainScreenViewModel(trackToMediaItemMapper)
            val mockedMediaItem = mockk<MainScreenViewModel.PlayerMediaItem>()
            every { trackToMediaItemMapper.mapTrackToMediaItem(any()) } returns mockedMediaItem

            viewModel.state.test {
                // When
                DummyTracksDataSource.addTrack(track1)
                // Then
                assertEquals(
                    MainScreenViewModel.UiState.Success(
                        tracks = listOf(
                            track1
                        ),
                        currentTrackIndex = 0,
                        playingMediaItem = mockedMediaItem
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Given queue contains 2 tracks, when second track gets selected then a new UiState Success is emitted with current track ID matching the second track's`() =
        runTest {
            // Given
            val viewModel = createMainScreenViewModel(trackToMediaItemMapper)
            val mockedMediaItem = mockk<MainScreenViewModel.PlayerMediaItem>()
            every { trackToMediaItemMapper.mapTrackToMediaItem(any()) } returns mockedMediaItem

            DummyTracksDataSource.addTrack(track1)
            DummyTracksDataSource.addTrack(track2)
            viewModel.state.test {
                // When
                viewModel.onQueueEvent(MainScreenViewModel.QueueEvent.TrackSelected(track2.id))
                // Then
                assertEquals(
                    MainScreenViewModel.UiState.Success(
                        tracks = listOf(
                            track1, track2
                        ),
                        currentTrackIndex = 1,
                        playingMediaItem = mockedMediaItem
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Given queue contains 2 tracks, when first track ends then a new UiState Success is emitted with current track ID matching the second track's`() =
        runTest {
            // Given
            val viewModel = createMainScreenViewModel(trackToMediaItemMapper)
            val mockedMediaItem = mockk<MainScreenViewModel.PlayerMediaItem>()
            every { trackToMediaItemMapper.mapTrackToMediaItem(any()) } returns mockedMediaItem

            DummyTracksDataSource.addTrack(track1)
            DummyTracksDataSource.addTrack(track2)
            viewModel.state.test {
                // When
                viewModel.onPlayerEvent(MainScreenViewModel.PlayerEvent.SelectedTrackEnded)
                // Then
                assertEquals(
                    MainScreenViewModel.UiState.Success(
                        tracks = listOf(
                            track1, track2
                        ),
                        currentTrackIndex = 1,
                        playingMediaItem = mockedMediaItem
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `Given queue contains 2 tracks, when player error occurs then current track is marked as not readable and second track is selected`() =
        runTest {
            // Given
            val viewModel = createMainScreenViewModel(trackToMediaItemMapper)
            val mockedMediaItem = mockk<MainScreenViewModel.PlayerMediaItem>()
            every { trackToMediaItemMapper.mapTrackToMediaItem(any()) } returns mockedMediaItem

            DummyTracksDataSource.addTrack(track1)
            DummyTracksDataSource.addTrack(track2)
            viewModel.state.test {
                // When
                viewModel.onPlayerEvent(MainScreenViewModel.PlayerEvent.Error(mockk<PlaybackException>()))
                // Then
                assertEquals(
                    MainScreenViewModel.UiState.Success(
                        tracks = listOf(
                            track1.copy(readable = false),
                            track2
                        ),
                        currentTrackIndex = 1,
                        playingMediaItem = mockedMediaItem
                    ),
                    expectMostRecentItem()
                )
            }
        }

    /**
     * FIXME: I couldn't get expectMostRecentItem() to receive the actual latest value emitted
     */
    @Test
    fun `Given queue contains 3 tracks and track 2 is being played, when deleting it then emits new state with track 3 sets as current track`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = createMainScreenViewModel(trackToMediaItemMapper, testDispatcher)
            val mockedMediaItem1or2 = mockk<MainScreenViewModel.PlayerMediaItem>()
            val mockedMediaItem3 = mockk<MainScreenViewModel.PlayerMediaItem>()
            every { trackToMediaItemMapper.mapTrackToMediaItem(track1) } returns mockedMediaItem1or2
            every { trackToMediaItemMapper.mapTrackToMediaItem(track2) } returns mockedMediaItem1or2
            every { trackToMediaItemMapper.mapTrackToMediaItem(track3) } returns mockedMediaItem3

            DummyTracksDataSource.addTrack(track1)
            DummyTracksDataSource.addTrack(track2)
            DummyTracksDataSource.addTrack(track3)

            viewModel.state.test {
                viewModel.onQueueEvent(MainScreenViewModel.QueueEvent.TrackSelected(track2.id))
                // When
                viewModel.onQueueEvent(MainScreenViewModel.QueueEvent.TrackRemovalRequest(track2.id))
                awaitItem()
                awaitItem()
                awaitItem()
                awaitItem()
                // Then
                assertEquals(
                    MainScreenViewModel.UiState.Success(
                        tracks = listOf(
                            track1,
                            track3
                        ),
                        currentTrackIndex = 1,
                        playingMediaItem = mockedMediaItem3
                    ),
                    awaitItem()
                )
            }
        }

    private fun createMainScreenViewModel(
        mockedTrackToMediaItemMapper: TrackToMediaItemMapper,
        dispatcher: CoroutineDispatcher = StandardTestDispatcher()
    ): MainScreenViewModel {
        val listeningQueueRepository = SimpleListeningQueueRepository(DummyTracksDataSource)
        return MainScreenViewModel(
            getListeningQueue = GetListeningQueueUseCase(listeningQueueRepository),
            removeTrackFromQueue = RemoveTrackFromQueueUseCase(listeningQueueRepository),
            trackToMediaItemMapper = mockedTrackToMediaItemMapper,
            coroutineDispatcher = dispatcher
        )
    }

    private val track1 = Track(
        id = 42,
        title = "title",
        durationInSeconds = 300,
        albumTitle = "album title",
        coverImageUrl = "coverImageUrl",
        artistName = "artistName",
        previewUrl = "previewUrl",
        readable = true,
    )

    private val track2 = Track(
        id = 88,
        title = "another title",
        durationInSeconds = 300,
        albumTitle = "another album title",
        coverImageUrl = "anotherCoverImageUrl",
        artistName = "AnotherArtistName",
        previewUrl = "anotherPreviewUrl",
        readable = true,
    )

    private val track3 = Track(
        id = 9000,
        title = "You got this",
        durationInSeconds = 300,
        albumTitle = "",
        coverImageUrl = "",
        artistName = "",
        previewUrl = "",
        readable = true,
    )

}