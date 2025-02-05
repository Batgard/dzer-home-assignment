package com.deezer.exoapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deezer.exoapplication.playlist.presentation.AllTracksActivity
import com.deezer.exoapplication.ui.theme.ExoAppTheme
import com.deezer.exoapplication.ui.theme.Size

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                        verticalArrangement = Arrangement.Bottom) {
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, AllTracksActivity::class.java))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            Text(text = "See all tracks")
                        }

                        Spacer(modifier = Modifier.height(Size.Spacing.Large))

                        Player(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(innerPadding)
            )
        }
    }
}