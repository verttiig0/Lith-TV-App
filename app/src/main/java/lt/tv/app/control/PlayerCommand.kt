package lt.tv.app.control

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Everything that can drive playback. The TV remote uses these today; the phone app will too. */
sealed interface PlayerCommand {
    data object Next : PlayerCommand
    data object Previous : PlayerCommand
    data class PlayChannel(val id: String) : PlayerCommand
    data object TogglePause : PlayerCommand
}

/** What the TV is doing right now, for a future phone app to monitor. */
data class PlaybackStatus(
    val channelId: String? = null,
    val channelName: String? = null,
    val playing: Boolean = false,
    val error: String? = null,
)

/**
 * In-process hub between inputs (remote keys, and later a LAN server for the phone app)
 * and the player. A network server only needs to call [send] and observe [status].
 */
object ControlBus {
    private val _commands = MutableSharedFlow<PlayerCommand>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val commands: SharedFlow<PlayerCommand> = _commands.asSharedFlow()

    private val _status = MutableStateFlow(PlaybackStatus())
    val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

    fun send(command: PlayerCommand) {
        _commands.tryEmit(command)
    }

    internal fun publish(status: PlaybackStatus) {
        _status.value = status
    }
}
