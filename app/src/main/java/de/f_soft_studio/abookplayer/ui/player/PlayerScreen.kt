package de.f_soft_studio.abookplayer.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.f_soft_studio.abookplayer.R

/**
 * Player-Oberfläche mit Slider, Geschwindigkeit und Schlaf-Timer.
 */
@Composable
fun PlayerRoute(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.screen_player_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = stringResource(id = R.string.screen_player_loading), style = MaterialTheme.typography.labelSmall)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(id = R.string.screen_player_chapter), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = state.positionMs.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..(state.durationMs?.toFloat() ?: 1f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(state.positionMs))
                        Text(text = state.durationMs?.let { formatTime(it) } ?: "--:--")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.seekBackward() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = stringResource(id = R.string.screen_player_rewind))
                }
                IconButton(onClick = { viewModel.playPause() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = stringResource(id = R.string.screen_player_title), modifier = Modifier.size(64.dp))
                }
                IconButton(onClick = { viewModel.seekForward() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = stringResource(id = R.string.screen_player_forward))
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(id = R.string.screen_player_speed))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        Button(onClick = { viewModel.updateSpeed(speed) }) {
                            Text(text = "${speed}x")
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(id = R.string.screen_player_sleep))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { viewModel.updateSleepTimer(null) }) {
                        Text(stringResource(id = R.string.sleep_timer_off))
                    }
                    listOf(10, 20, 30).forEach { minutes ->
                        Button(onClick = { viewModel.updateSleepTimer(minutes * 60_000L) }) {
                            Text(text = "$minutes min")
                        }
                    }
                }
                state.sleepTimerRemainingMs?.let { remaining ->
                    Text(
                        text = "${stringResource(id = R.string.screen_player_sleep)}: ${formatTime(remaining)}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
