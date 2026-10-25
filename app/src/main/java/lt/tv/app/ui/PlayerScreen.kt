package lt.tv.app.ui

import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import lt.tv.app.control.PlayerCommand

private const val DEFAULT_USER_AGENT = "LietuvosTV/0.1 (Android TV)"

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(viewModel: TvViewModel, acceptKeys: Boolean) {
    val context = LocalContext.current
    val channel by viewModel.current.collectAsStateWithLifecycle()
    val visible by viewModel.visibleChannels.collectAsStateWithLifecycle()
    val paused by viewModel.paused.collectAsStateWithLifecycle()
    var error by remember { mutableStateOf<String?>(null) }
    var showBanner by remember { mutableStateOf(true) }
    var audioOnly by remember { mutableStateOf(false) }

    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) error = null
                viewModel.onPlaybackChanged(isPlaying, error)
            }

            override fun onTracksChanged(tracks: Tracks) {
                audioOnly = !tracks.isEmpty && !tracks.containsType(C.TRACK_TYPE_VIDEO)
            }

            override fun onPlayerError(e: PlaybackException) {
                error = "This channel can't be played right now. It may be offline or only available in Lithuania."
                viewModel.onPlaybackChanged(false, e.errorCodeName)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // Stop streaming when the app goes to the background, resume when it comes back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (player.mediaItemCount > 0) player.prepare()
                Lifecycle.Event.ON_STOP -> player.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(channel?.id, channel?.url, channel?.userAgent) {
        error = null
        val ch = channel
        if (ch == null) {
            player.stop()
            return@LaunchedEffect
        }
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent(ch.userAgent ?: DEFAULT_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
        player.setMediaSource(DefaultMediaSourceFactory(http).createMediaSource(MediaItem.fromUri(ch.url)))
        player.prepare()
        player.playWhenReady = true
        showBanner = true
        delay(4_000)
        showBanner = false
    }

    LaunchedEffect(paused) { player.playWhenReady = !paused }

    val focus = remember { FocusRequester() }
    LaunchedEffect(acceptKeys) { if (acceptKeys) focus.requestFocus() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focus)
            .onKeyEvent { event ->
                if (!acceptKeys || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_DOWN -> viewModel.handle(PlayerCommand.Previous)
                    KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_UP -> viewModel.handle(PlayerCommand.Next)
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_LEFT ->
                        viewModel.show(Screen.ChannelList)
                    KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS -> viewModel.show(Screen.Manage)
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE ->
                        viewModel.handle(PlayerCommand.TogglePause)
                    KeyEvent.KEYCODE_DPAD_RIGHT -> showBanner = true
                    else -> return@onKeyEvent false
                }
                true
            }
            .focusable(enabled = acceptKeys),
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).also { it.useController = false; it.player = player } },
            modifier = Modifier.fillMaxSize(),
        )

        // Radio streams have no picture, so show the station name instead of a black screen.
        if (audioOnly && channel != null) {
            Text(
                channel?.name.orEmpty(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = (showBanner || error != null) && channel != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart).padding(48.dp),
        ) {
            val number = visible.indexOfFirst { it.id == channel?.id } + 1
            Column(
                Modifier
                    .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text("$number  ${channel?.name.orEmpty()}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                error?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFFFB4AB)) }
                Text(
                    "▲▼ change channel · OK channel list · Menu manage channels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBBBBBB),
                )
            }
        }

        if (visible.isEmpty()) {
            Text(
                "No channels to show. Press Menu to add or unhide channels.",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }
    }
}
