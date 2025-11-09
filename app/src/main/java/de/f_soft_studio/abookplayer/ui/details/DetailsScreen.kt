package de.f_soft_studio.abookplayer.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.f_soft_studio.abookplayer.R

/**
 * Detailansicht eines Hörbuchs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsRoute(
    onPlayChapter: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.screen_details_title)) })
        }
    ) { padding ->
        state.book?.let { bookWithChapters ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = bookWithChapters.book.title, style = MaterialTheme.typography.headlineSmall)
                Text(text = bookWithChapters.book.author ?: "", style = MaterialTheme.typography.bodyMedium)
                Divider()
                Text(text = stringResource(id = R.string.screen_details_toc), style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(bookWithChapters.chapters) { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = chapter.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = formatDuration(chapter.durationMs))
                            }
                            Button(onClick = { onPlayChapter(chapter.id) }) {
                                Text(stringResource(id = R.string.screen_details_play))
                            }
                        }
                    }
                }
                Divider()
                Text(text = stringResource(id = R.string.screen_player_bookmark), style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.noteDraft,
                        onValueChange = viewModel::updateNote,
                        label = { Text(stringResource(id = R.string.screen_details_note)) }
                    )
                    OutlinedTextField(
                        value = (state.positionDraft / 1000).toString(),
                        onValueChange = { value -> viewModel.updatePosition(value.toLongOrNull()?.times(1000) ?: 0) },
                        label = { Text(stringResource(id = R.string.screen_details_position)) }
                    )
                }
                Button(onClick = {
                    val firstChapter = bookWithChapters.chapters.firstOrNull()
                    if (firstChapter != null) {
                        viewModel.saveBookmark(firstChapter.id)
                    }
                }) {
                    Text(stringResource(id = R.string.screen_details_add_bookmark))
                }
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.bookmarks) { bookmark ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = bookmark.note, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(text = formatDuration(bookmark.positionMs))
                            }
                            TextButton(onClick = { viewModel.deleteBookmark(bookmark) }) {
                                Text(text = stringResource(id = R.string.screen_details_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
