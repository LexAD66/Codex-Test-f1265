package de.f_soft_studio.abookplayer.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import de.f_soft_studio.abookplayer.R
import de.f_soft_studio.abookplayer.domain.usecase.GetLibraryFlowUseCase

/**
 * Bildschirm für die Bibliothek inklusive Suchfeld und Filterchips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryRoute(
    onBookSelected: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.startScan()
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.updateSearch(it) },
                onSearch = { viewModel.updateSearch(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text(text = stringResource(id = R.string.screen_library_search)) }
            ) {}
            FilterRow(
                current = state.filter,
                onFilterChange = viewModel::updateFilter
            )
            when (val progress = state.scanProgress) {
                is de.f_soft_studio.abookplayer.data.repo.ScannerRepository.ScanProgress.Running -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(id = R.string.scanner_progress),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is de.f_soft_studio.abookplayer.data.repo.ScannerRepository.ScanProgress.Failed -> {
                    Text(
                        text = stringResource(id = R.string.scanner_failed),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is de.f_soft_studio.abookplayer.data.repo.ScannerRepository.ScanProgress.Finished -> {
                    if (progress.books.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.scanner_found),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                else -> Unit
            }
            if (state.books.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.screen_library_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.books) { book ->
                        LibraryItemRow(
                            title = book.book.title,
                            subtitle = book.book.author ?: book.book.series ?: "",
                            coverUri = book.book.coverUri,
                            onClick = { onBookSelected(book.book.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    current: GetLibraryFlowUseCase.Filter,
    onFilterChange: (GetLibraryFlowUseCase.Filter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = stringResource(id = R.string.screen_library_title),
            selected = current == GetLibraryFlowUseCase.Filter.ALLE,
            onClick = { onFilterChange(GetLibraryFlowUseCase.Filter.ALLE) }
        )
        FilterChip(
            text = stringResource(id = R.string.screen_library_recent),
            selected = current == GetLibraryFlowUseCase.Filter.ZULETZT,
            onClick = { onFilterChange(GetLibraryFlowUseCase.Filter.ZULETZT) }
        )
        FilterChip(
            text = stringResource(id = R.string.screen_library_completed),
            selected = current == GetLibraryFlowUseCase.Filter.ABGESCHLOSSEN,
            onClick = { onFilterChange(GetLibraryFlowUseCase.Filter.ABGESCHLOSSEN) }
        )
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun LibraryItemRow(
    title: String,
    subtitle: String,
    coverUri: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val painter = coverUri?.let { uri ->
            rememberAsyncImagePainter(model = uri)
        } ?: painterResource(id = R.drawable.ic_launcher_foreground)
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.height(72.dp),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
