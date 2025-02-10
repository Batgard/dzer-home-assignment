package com.deezer.exoapplication.core.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.deezer.exoapplication.R
import com.deezer.exoapplication.ui.theme.Size

@Composable
fun TrackImage(imageUrl: String, modifier: Modifier = Modifier.size(Size.Image.Medium)) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Track Cover",
        modifier = modifier,
        placeholder = painterResource(id = R.drawable.vinyl),
        error = painterResource(id = R.drawable.vinyl_broken)
    )
}