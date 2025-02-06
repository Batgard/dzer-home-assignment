package com.deezer.exoapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.deezer.exoapplication.player.presentation.PlayerViewModel
import com.deezer.exoapplication.playlist.presentation.AllTracksActivity
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        AllTracksActivity::class.java
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            Text(text = "See all tracks")
                        }

                        Spacer(modifier = Modifier.height(Size.Spacing.Large))

                        Player(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(innerPadding)
            )
        }
    }
}