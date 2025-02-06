package com.deezer.exoapplication

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deezer.exoapplication.player.presentation.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun Player(
    state: PlayerViewModel.UiState,
    onPlayerEvent: (PlayerViewModel.PlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {

    val currentContext = LocalContext.current

    val player: ExoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(currentContext).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            onPlayerEvent(PlayerViewModel.PlayerEvent.Error(error))
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            Log.d("Player", "onMediaItemTransition. MediaItem ID == ${mediaItem?.mediaId} for reason $reason")
                            onPlayerEvent(
                                PlayerViewModel.PlayerEvent.SelectedTrackChanged(
                                    mediaItem?.mediaId
                                        ?: throw IllegalArgumentException("mediaId cannot be null")
                                )
                            )
                        }

                        override fun onEvents(player: Player, events: Player.Events) {
                            super.onEvents(player, events)
                        }
                    }
                )
                if (state is PlayerViewModel.UiState.Success) {
                    setMediaItems(state.mediaItems)
                    prepare()
                    play()
                }
            }
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setShowShuffleButton(true)
                setPlayer(player)
            }
        },
        update = {
            if (state is PlayerViewModel.UiState.Success) {
                player.setMediaItems(state.mediaItems)
                player.seekTo(state.currentTrackIndex, 0L)
                player.prepare()
                player.play()
            }
        },
        onRelease = {
            player.release()
        }
    )
}
