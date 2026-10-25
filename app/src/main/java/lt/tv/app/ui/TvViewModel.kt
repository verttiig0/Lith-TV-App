package lt.tv.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lt.tv.app.control.ControlBus
import lt.tv.app.control.PlaybackStatus
import lt.tv.app.control.PlayerCommand
import lt.tv.app.data.Channel
import lt.tv.app.data.ChannelRepository

enum class Screen { Player, ChannelList, Manage }

class TvViewModel(app: Application) : AndroidViewModel(app) {

    val repository = ChannelRepository(app)

    val allChannels: StateFlow<List<Channel>> =
        repository.channels.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val visibleChannels: StateFlow<List<Channel>> =
        allChannels.map { list -> list.filterNot { it.hidden } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val selectedId = MutableStateFlow<String?>(null)

    /** The playing channel; falls back to the first visible one, so startup plays channel 1. */
    val current: StateFlow<Channel?> =
        combine(visibleChannels, selectedId) { visible, id -> visible.firstOrNull { it.id == id } ?: visible.firstOrNull() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _screen = MutableStateFlow(Screen.Player)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _paused = MutableStateFlow(false)
    val paused: StateFlow<Boolean> = _paused.asStateFlow()

    init {
        viewModelScope.launch {
            ControlBus.commands.collect { handle(it) }
        }
    }

    fun handle(command: PlayerCommand) {
        when (command) {
            PlayerCommand.Next -> step(1)
            PlayerCommand.Previous -> step(-1)
            is PlayerCommand.PlayChannel -> play(command.id)
            PlayerCommand.TogglePause -> _paused.value = !_paused.value
        }
    }

    fun play(id: String) {
        selectedId.value = id
        _paused.value = false
    }

    private fun step(delta: Int) {
        val visible = visibleChannels.value
        if (visible.isEmpty()) return
        val index = visible.indexOfFirst { it.id == current.value?.id }.coerceAtLeast(0)
        play(visible[Math.floorMod(index + delta, visible.size)].id)
    }

    fun show(screen: Screen) {
        _screen.value = screen
    }

    fun onPlaybackChanged(playing: Boolean, error: String?) {
        val channel = current.value
        ControlBus.publish(PlaybackStatus(channel?.id, channel?.name, playing, error))
    }

    fun setHidden(id: String, hidden: Boolean) = viewModelScope.launch { repository.setHidden(id, hidden) }
    fun remove(id: String) = viewModelScope.launch { repository.remove(id) }
    fun move(id: String, offset: Int) = viewModelScope.launch { repository.move(id, offset) }
    fun add(name: String, url: String, userAgent: String?) = viewModelScope.launch { repository.add(name, url, userAgent) }
    fun edit(id: String, name: String, url: String, userAgent: String?) =
        viewModelScope.launch { repository.edit(id, name, url, userAgent) }
    fun resetToDefaults() = viewModelScope.launch { repository.resetToDefaults() }
}
