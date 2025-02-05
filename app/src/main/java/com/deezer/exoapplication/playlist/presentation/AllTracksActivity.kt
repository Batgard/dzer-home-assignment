package com.deezer.exoapplication.playlist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.deezer.exoapplication.R
import com.deezer.exoapplication.playlist.domain.Track
import com.deezer.exoapplication.ui.theme.ExoAppTheme

class AllTracksActivity : ComponentActivity() {

    private val viewModel: TrackListViewModel by viewModels { TrackListViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            ExoAppTheme {
                Scaffold { contentPadding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)) {
                        when (state) {
                            TrackListViewModel.UiState.Loading -> LoadingScreen()
                            TrackListViewModel.UiState.Empty -> EmptyScreen()
                            is TrackListViewModel.UiState.Error -> ErrorScreen()
                            is TrackListViewModel.UiState.Success -> TrackListScreen(
                                trackList = (state as TrackListViewModel.UiState.Success).tracks,
                                onTrackClick = {
                                    viewModel.onTrackClick(it)
                                },
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Text(text = "Loading")
}

@Composable
fun EmptyScreen() {
    Text(text = "No tracks :(")
}

@Composable
fun ErrorScreen() {
    Text(text = "Error")
}

@Composable
fun TrackListScreen(
    trackList: List<Track>,
    onTrackClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(trackList) { track ->
            TrackItem(track = track, onTrackClick = onTrackClick)
            if (track != trackList.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    onTrackClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick(track.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackImage(track.coverImageUrl)
            Spacer(modifier = Modifier.width(16.dp))
            TrackInfo(track)
        }
    }
}

@Composable
fun TrackImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Track Cover",
        modifier = Modifier.size(64.dp),
        placeholder = painterResource(id = R.drawable.vinyl),
        error = painterResource(id = R.drawable.vinyl_broken)
    )
}

@Composable
fun TrackInfo(track: Track) {
    Column {
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artistName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun TrackItemPreview() {
    ExoAppTheme {
        TrackItem(
            track = Track(
                id = 1,
                title = "Track Title",
                durationInSeconds = 180,
                coverImageUrl = "https://example.com/cover.jpg",
                artistName = "Artist Name",
                albumTitle = "Album Title",
            ),
            onTrackClick = {}
        )
    }
}
