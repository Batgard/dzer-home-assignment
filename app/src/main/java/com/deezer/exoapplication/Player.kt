package com.deezer.exoapplication

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deezer.exoapplication.player.presentation.MainScreenViewModel

@OptIn(UnstableApi::class)
@Composable
fun Player(
    mediaItem: MainScreenViewModel.PlayerMediaItem?,
    onPlayerEvent: (MainScreenViewModel.PlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentContext = LocalContext.current

    val onPlayerEvents by rememberUpdatedState(onPlayerEvent)
    val playerListener: Player.Listener by remember {
        mutableStateOf(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    onPlayerEvents(MainScreenViewModel.PlayerEvent.Error(error))
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    val mediaId = player.currentMediaItem?.mediaId ?: -1
                    Log.d(
                        "Player",
                        "onMediaItemTransition. MediaItem ID == $mediaId for reason $events"
                    )
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d("Player", "onPlaybackStateChanged. State == $playbackState")
                    if (playbackState == Player.STATE_ENDED) {
                        onPlayerEvents(
                            MainScreenViewModel.PlayerEvent.SelectedTrackEnded
                        )
                    }
                }
            })
    }
    val player: ExoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(currentContext).build().apply {
                addListener(playerListener)
                if (mediaItem != null) {
                    prepare()
                    play()
                }
            }
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Log.d("Player", "factory")
            PlayerView(context).apply {
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setShowShuffleButton(false)
                setPlayer(player)
            }
        },
        update = {
            Log.d("Player", "update")
            if (mediaItem != null) {
                player.setMediaItem(mediaItem.mediaItem)
                player.prepare()
                player.play()
            }
        },
        onRelease = {
            player.release()
        }
    )
}
