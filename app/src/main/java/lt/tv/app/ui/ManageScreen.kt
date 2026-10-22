package lt.tv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import lt.tv.app.data.Channel
import androidx.compose.material3.MaterialTheme as PhoneMaterialTheme
import androidx.compose.material3.Text as PhoneText
import androidx.compose.material3.darkColorScheme as phoneDarkColorScheme

/** Reorder, hide, remove and add channels. The first visible channel plays on startup. */
@Composable
fun ManageScreen(viewModel: TvViewModel) {
    val channels by viewModel.allChannels.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmRemoveId by remember { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xF2101418))
            .padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Manage channels", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Button(onClick = { adding = true }, modifier = Modifier.focusRequester(focus)) { Text("Add channel") }
            OutlinedButton(onClick = { viewModel.resetToDefaults() }) { Text("Restore defaults") }
            OutlinedButton(onClick = { viewModel.show(Screen.Player) }) { Text("Done") }
        }
        Text(
            "The first channel that isn't hidden plays when the app starts.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFBBBBBB),
            modifier = Modifier.padding(vertical = 12.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(channels, key = { _, ch -> ch.id }) { index, ch ->
                ChannelRow(
                    index = index,
                    channel = ch,
                    isLast = index == channels.lastIndex,
                    confirmingRemove = confirmRemoveId == ch.id,
                    onToggleHidden = { viewModel.setHidden(ch.id, !ch.hidden) },
                    onMove = { viewModel.move(ch.id, it) },
                    onRemove = {
                        if (confirmRemoveId == ch.id) {
                            viewModel.remove(ch.id)
                            confirmRemoveId = null
                        } else {
                            confirmRemoveId = ch.id
                        }
                    },
                )
            }
        }
    }

    if (adding) {
        AddChannelDialog(
            onDismiss = { adding = false },
            onAdd = { name, url, ua ->
                viewModel.add(name, url, ua)
                adding = false
            },
        )
    }
}

@Composable
private fun ChannelRow(
    index: Int,
    channel: Channel,
    isLast: Boolean,
    confirmingRemove: Boolean,
    onToggleHidden: () -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.width(420.dp)) {
            Text(
                "${index + 1}. ${channel.name}" + if (channel.hidden) "  (hidden)" else "",
                style = MaterialTheme.typography.titleMedium,
                color = if (channel.hidden) Color(0xFF888888) else Color.White,
            )
            Text(
                channel.url,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedButton(onClick = onToggleHidden) { Text(if (channel.hidden) "Show" else "Hide") }
        OutlinedButton(onClick = { onMove(-1) }, enabled = index > 0) { Text("Move up") }
        OutlinedButton(onClick = { onMove(1) }, enabled = !isLast) { Text("Move down") }
        OutlinedButton(onClick = onRemove) { Text(if (confirmingRemove) "Press again to remove" else "Remove") }
    }
}

@Composable
private fun AddChannelDialog(onDismiss: () -> Unit, onAdd: (String, String, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var userAgent by rememberSaveable { mutableStateOf("") }
    val valid = name.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))

    PhoneMaterialTheme(colorScheme = phoneDarkColorScheme()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { PhoneText("Add channel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { PhoneText("Name") }, singleLine = true)
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { PhoneText("Stream URL (.m3u8 or .mpd)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = userAgent,
                        onValueChange = { userAgent = it },
                        label = { PhoneText("User-Agent (optional)") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onAdd(name, url, userAgent) }, enabled = valid) { PhoneText("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { PhoneText("Cancel") } },
        )
    }
}
