package lt.tv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** Left-hand channel picker shown over the playing video. */
@Composable
fun ChannelListOverlay(viewModel: TvViewModel) {
    val channels by viewModel.visibleChannels.collectAsStateWithLifecycle()
    val current by viewModel.current.collectAsStateWithLifecycle()
    val currentIndex = channels.indexOfFirst { it.id == current?.id }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(Modifier.fillMaxSize().background(Color(0x66000000))) {
        Column(
            Modifier
                .width(380.dp)
                .fillMaxHeight()
                .background(Color(0xEE101418))
                .padding(vertical = 24.dp, horizontal = 16.dp),
        ) {
            Text("Channels", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            LazyColumn(state = listState, modifier = Modifier.padding(top = 12.dp)) {
                itemsIndexed(channels, key = { _, ch -> ch.id }) { index, ch ->
                    ListItem(
                        selected = ch.id == current?.id,
                        onClick = {
                            viewModel.play(ch.id)
                            viewModel.show(Screen.Player)
                        },
                        headlineContent = { Text(ch.name) },
                        leadingContent = { Text("${index + 1}") },
                        modifier = if (index == currentIndex) Modifier.focusRequester(focus) else Modifier,
                    )
                }
                item(key = "manage") {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.show(Screen.Manage) },
                        headlineContent = { Text("Manage channels…") },
                        modifier = if (channels.isEmpty()) Modifier.focusRequester(focus) else Modifier,
                    )
                }
            }
        }
    }
}
