package lt.tv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import lt.tv.app.data.UserAgents
import androidx.compose.material3.MaterialTheme as PhoneMaterialTheme
import androidx.compose.material3.Text as PhoneText
import androidx.compose.material3.darkColorScheme as phoneDarkColorScheme

/** Reorder, hide, remove and add channels. The first visible channel plays on startup. */
@Composable
fun ManageScreen(viewModel: TvViewModel) {
    val channels by viewModel.allChannels.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Channel?>(null) }
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
            Button(onClick = { adding = true }, modifier = Modifier.focusRequester(focus).onTap { adding = true }) { Text("Add channel") }
            OutlinedButton(onClick = { viewModel.resetToDefaults() }, modifier = Modifier.onTap { viewModel.resetToDefaults() }) { Text("Restore defaults") }
            OutlinedButton(onClick = { viewModel.show(Screen.Player) }, modifier = Modifier.onTap { viewModel.show(Screen.Player) }) { Text("Done") }
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
                    onEdit = { editing = ch },
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
        ChannelDialog(
            title = "Add channel",
            confirmLabel = "Add",
            onDismiss = { adding = false },
            onConfirm = { name, url, ua ->
                viewModel.add(name, url, ua)
                adding = false
            },
        )
    }
    editing?.let { ch ->
        ChannelDialog(
            title = "Edit channel",
            confirmLabel = "Save",
            initial = ch,
            onDismiss = { editing = null },
            onConfirm = { name, url, ua ->
                viewModel.edit(ch.id, name, url, ua)
                editing = null
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
    onEdit: () -> Unit,
    onToggleHidden: () -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
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
        OutlinedButton(onClick = onEdit, modifier = Modifier.onTap(onEdit)) { Text("Edit", softWrap = false) }
        OutlinedButton(onClick = onToggleHidden, modifier = Modifier.onTap(onToggleHidden)) { Text(if (channel.hidden) "Show" else "Hide", softWrap = false) }
        OutlinedButton(onClick = { onMove(-1) }, enabled = index > 0, modifier = Modifier.onTap { if (index > 0) onMove(-1) }) { Text("Move up", softWrap = false) }
        OutlinedButton(onClick = { onMove(1) }, enabled = !isLast, modifier = Modifier.onTap { if (!isLast) onMove(1) }) { Text("Move down", softWrap = false) }
        OutlinedButton(onClick = onRemove, modifier = Modifier.onTap(onRemove)) { Text(if (confirmingRemove) "Press again to remove" else "Remove", softWrap = false) }
    }
}

@Composable
private fun ChannelDialog(
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit,
    initial: Channel? = null,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var url by rememberSaveable { mutableStateOf(initial?.url ?: "http://") }
    var userAgent by rememberSaveable { mutableStateOf(initial?.userAgent ?: "") }
    var customAgent by rememberSaveable {
        mutableStateOf(userAgent.isNotEmpty() && UserAgents.presets.none { it.second == userAgent })
    }
    var agentMenuOpen by remember { mutableStateOf(false) }
    val agentLabel = when {
        customAgent -> "Custom"
        userAgent.isEmpty() -> "Default"
        else -> UserAgents.presets.first { it.second == userAgent }.first
    }
    val valid = name.isNotBlank() && Regex("https?://.+").matches(url.trim())

    PhoneMaterialTheme(colorScheme = phoneDarkColorScheme()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { PhoneText(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { PhoneText("Name") }, singleLine = true)
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { PhoneText("Stream URL (.m3u8 or .mpd)") },
                        singleLine = true,
                    )
                    Box {
                        TextButton(onClick = { agentMenuOpen = true }) { PhoneText("User-Agent: $agentLabel ▾") }
                        DropdownMenu(expanded = agentMenuOpen, onDismissRequest = { agentMenuOpen = false }) {
                            val pick = { ua: String, custom: Boolean ->
                                userAgent = ua
                                customAgent = custom
                                agentMenuOpen = false
                            }
                            DropdownMenuItem(text = { PhoneText("Default") }, onClick = { pick("", false) })
                            UserAgents.presets.forEach { (label, ua) ->
                                DropdownMenuItem(text = { PhoneText(label) }, onClick = { pick(ua, false) })
                            }
                            DropdownMenuItem(text = { PhoneText("Custom…") }, onClick = { pick(userAgent, true) })
                        }
                    }
                    if (customAgent) {
                        OutlinedTextField(
                            value = userAgent,
                            onValueChange = { userAgent = it },
                            label = { PhoneText("Custom User-Agent") },
                            singleLine = true,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(name, url, userAgent) }, enabled = valid) { PhoneText(confirmLabel) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { PhoneText("Cancel") } },
        )
    }
}
