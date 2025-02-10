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
import com.deezer.exoapplication.player.presentation.MainScreenViewModel

@OptIn(UnstableApi::class)
@Composable
fun Player(
    state: MainScreenViewModel.UiState,
    onPlayerEvent: (MainScreenViewModel.PlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentContext = LocalContext.current

    val player: ExoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(currentContext).build().apply {
                addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            onPlayerEvent(MainScreenViewModel.PlayerEvent.Error(error))
                        }

                        override fun onEvents(player: Player, events: Player.Events) {
                            val mediaId = if (player.mediaItemCount > 0) player.getMediaItemAt(0).mediaId else {-1}
                            Log.d("Player", "onMediaItemTransition. MediaItem ID == $mediaId for reason $events")
                            super.onEvents(player, events)
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            Log.d("Player", "onPlaybackStateChanged. State == $playbackState")
                            if (playbackState == Player.STATE_ENDED) {
                                onPlayerEvent(
                                    MainScreenViewModel.PlayerEvent.SelectedTrackEnded
                                )
                            }
                        }
                    }
                )
                if (state is MainScreenViewModel.UiState.Success) {
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
                setShowShuffleButton(false)
                setPlayer(player)
            }
        },
        update = {
            if (state is MainScreenViewModel.UiState.Success) {
                player.setMediaItem(state.playingMediaItem)
                player.prepare()
                player.play()
            }
        },
        onRelease = {
            player.release()
        }
    )
}
