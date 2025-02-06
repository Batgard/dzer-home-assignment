package com.deezer.exoapplication

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deezer.exoapplication.player.presentation.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun Player(
    state: PlayerViewModel.UiState,
    modifier: Modifier = Modifier,
) {

    val currentContext = LocalContext.current

    val player: ExoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(currentContext).build().apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
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
                player.prepare()
                player.play()
            }
        },
        onRelease = {
            player.release()
        }
    )
}