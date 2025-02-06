package com.deezer.exoapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.deezer.exoapplication.player.presentation.PlayerViewModel
import com.deezer.exoapplication.playlist.presentation.AllTracksActivity
import com.deezer.exoapplication.playlist.presentation.TrackImage
import com.deezer.exoapplication.ui.theme.ExoAppTheme
import com.deezer.exoapplication.ui.theme.Size

@UnstableApi
class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels { PlayerViewModel.Factory }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle(PlayerViewModel.UiState.Empty)
            ExoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            (state as? PlayerViewModel.UiState.Success)?.let { state1 ->
                                val rowState = rememberLazyListState()

                                LaunchedEffect(state1.currentTrackIndex) {
                                    Log.d("Player", "Scrolling to ${state1.currentTrackIndex}")
                                    rowState.scrollToItem(state1.currentTrackIndex)
                                }

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    state = rowState
                                ) {
                                    items((state as PlayerViewModel.UiState.Success).tracks) {
                                        val configuration = LocalConfiguration.current
                                        val cardWidth: Dp by remember {
                                            derivedStateOf {
                                                (configuration.screenWidthDp.toFloat() * 0.8).dp
                                            }
                                        }

                                        Card(
                                            modifier = Modifier
                                                .width(cardWidth)
                                                .aspectRatio(1f)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                TrackImage(
                                                    it.coverImageUrl,
                                                    modifier = Modifier.fillParentMaxSize()
                                                )
                                                if (it.readable.not()) {
                                                    Log.d(
                                                        "Player",
                                                        "$it has been marked as unplayable"
                                                    )
                                                    Text("Preview can't be played :(")
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(Size.Spacing.Medium))
                                    }
                                }

                            }

                            Spacer(modifier = Modifier.height(Size.Spacing.Large))

                            Player(
                                state = state,
                                onPlayerEvent = viewModel::onPlayerEvent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                        FloatingActionButton(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        AllTracksActivity::class.java
                                    )
                                )
                            },
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(Size.Spacing.Large)
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Add),
                                contentDescription = "All tracks to queue"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    ExoAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Player(
                state = PlayerViewModel.UiState.Empty,
                onPlayerEvent = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(innerPadding)
            )
        }
    }
}