package com.deezer.exoapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.deezer.exoapplication.player.presentation.PlayerViewModel
import com.deezer.exoapplication.playlist.domain.Track
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
            MainScreen(
                state = state,
                onPlayerEvent = viewModel::onPlayerEvent,
                onQueueEvent = viewModel::onQueueEvent
            )
        }
    }


}

@Composable
private fun MainScreen(
    state: PlayerViewModel.UiState,
    onPlayerEvent: (PlayerViewModel.PlayerEvent) -> Unit,
    onQueueEvent: (PlayerViewModel.QueueEvent) -> Unit
) {
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
                    (state as? PlayerViewModel.UiState.Success)?.let { successState ->
                        TrackQueue(successState, onQueueEvent)
                    }

                    Spacer(modifier = Modifier.height(Size.Spacing.Large))

                    Player(
                        state = state,
                        onPlayerEvent = onPlayerEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                GoToTrackListButton()
            }
        }
    }
}

@Composable
private fun GoToTrackListButton() {
    val context = LocalContext.current
    FloatingActionButton(
        onClick = {
            context.startActivity(Intent(context, AllTracksActivity::class.java))
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

@Composable
private fun TrackQueue(
    successState: PlayerViewModel.UiState.Success,
    onQueueEvent: (PlayerViewModel.QueueEvent) -> Unit
) {
    val rowState = rememberLazyListState()

    LaunchedEffect(successState.currentTrackIndex) {
        Log.d(
            "Player",
            "Scrolling to ${successState.currentTrackIndex}"
        )
        rowState.scrollToItem(successState.currentTrackIndex)
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = rowState
    ) {
        items(successState.tracks) {
            val configuration = LocalConfiguration.current
            val cardWidth: Dp by remember {
                derivedStateOf {
                    (configuration.screenWidthDp.toFloat() * 0.8).dp
                }
            }

            TrackCard(
                track = it,
                onQueueEvent = onQueueEvent,
                modifier = Modifier.width(cardWidth)
            )
            Spacer(modifier = Modifier.width(Size.Spacing.Medium))
        }
    }
}

@Composable
private fun TrackCard(
    track: Track,
    onQueueEvent: (PlayerViewModel.QueueEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable {
                onQueueEvent(
                    PlayerViewModel.QueueEvent.TrackSelected(
                        track.id
                    )
                )
            }) {
            TrackImage(
                track.coverImageUrl,
                modifier = Modifier.fillMaxSize()
            )
            Row(modifier = Modifier.fillMaxWidth()) {

                if (track.readable.not()) {
                    Log.d(
                        "Player",
                        "$track has been marked as unplayable"
                    )
                    Text(
                        "Preview can't be played :(",
                        Modifier.background(
                            MaterialTheme.colorScheme.background
                        )
                    )
                }
                Spacer(modifier = Modifier.width(Size.Spacing.Medium))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Button(
                        onClick = {
                            onQueueEvent(
                                PlayerViewModel.QueueEvent.TrackRemovalRequest(
                                    track.id
                                )
                            )
                        },
                        modifier = Modifier.size(Size.Button.Medium),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                    ) {
                        Icon(
                            painter = rememberVectorPainter(
                                Icons.Default.Close
                            ),
                            modifier = Modifier.size(Size.Icon.Large),
                            contentDescription = "Remove from queue"
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenEmptyPreview() {
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

@Preview
@Composable
fun MainScreenWithTracksPreview() {
    ExoAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Player(
                state = PlayerViewModel.UiState.Success(
                    tracks = listOf(
                        Track(
                            id = 42,
                            title = "Show must go on",
                            durationInSeconds = 180,
                            coverImageUrl = "https://images.fineartamerica.com/images/artworkimages/mediumlarge/2/show-must-go-on-queen-gina-dsgn.jpg",
                            artistName = "Queen",
                            albumTitle = "The Game",
                            readable = true,
                            previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                        ),
                        Track(
                            id = 88,
                            title = "There'd Better Be a Mirrorball",
                            durationInSeconds = 180,
                            coverImageUrl = "https://i.pinimg.com/736x/ec/e2/56/ece256ac6c6a40fd8fb0e2894848c66a.jpg",
                            artistName = "Arctic Monkeys",
                            albumTitle = "The car",
                            readable = true,
                            previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                        ),
                    ),
                    currentTrackIndex = 0,
                    mediaItems = emptyList()
                ),
                onPlayerEvent = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(innerPadding)
            )
        }
    }
}