package lt.tv.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "channels")

/** The user's channel line-up: order, hidden flags, removed defaults and custom channels. */
@Serializable
private data class StoredLineup(
    val channels: List<Channel> = emptyList(),
    /** Default channel ids the user removed, so app updates don't bring them back. */
    val removedDefaults: Set<String> = emptySet(),
)

class ChannelRepository(private val context: Context) {

    private val key = stringPreferencesKey("lineup")
    private val json = Json { ignoreUnknownKeys = true }

    /** All channels in user order, including hidden ones. */
    val channels: Flow<List<Channel>> = context.dataStore.data.map { it.lineup().merged().channels }

    suspend fun setHidden(id: String, hidden: Boolean) = update { lineup ->
        lineup.copy(channels = lineup.channels.map { if (it.id == id) it.copy(hidden = hidden) else it })
    }

    suspend fun remove(id: String) = update { lineup ->
        val removed = lineup.channels.firstOrNull { it.id == id } ?: return@update lineup
        lineup.copy(
            channels = lineup.channels - removed,
            removedDefaults = if (removed.custom) lineup.removedDefaults else lineup.removedDefaults + id,
        )
    }

    suspend fun add(name: String, url: String, userAgent: String? = null) = update { lineup ->
        val channel = Channel(
            id = "custom-" + UUID.randomUUID(),
            name = name.trim(),
            url = url.trim(),
            custom = true,
            userAgent = userAgent?.trim()?.takeIf { it.isNotEmpty() },
        )
        lineup.copy(channels = lineup.channels + channel)
    }

    suspend fun edit(id: String, name: String, url: String, userAgent: String?) = update { lineup ->
        lineup.copy(channels = lineup.channels.map {
            if (it.id == id) it.copy(name = name.trim(), url = url.trim(), userAgent = userAgent?.trim()?.takeIf { ua -> ua.isNotEmpty() }) else it
        })
    }

    /** Moves a channel by [offset] places. The first visible channel plays on startup. */
    suspend fun move(id: String, offset: Int) = update { lineup ->
        val list = lineup.channels.toMutableList()
        val from = list.indexOfFirst { it.id == id }
        if (from < 0) return@update lineup
        val to = (from + offset).coerceIn(0, list.lastIndex)
        list.add(to, list.removeAt(from))
        lineup.copy(channels = list)
    }

    /** Restores the built-in list; custom channels are kept at the end. */
    suspend fun resetToDefaults() = update { lineup ->
        StoredLineup(channels = DefaultChannels.all + lineup.channels.filter { it.custom })
    }

    private suspend fun update(transform: (StoredLineup) -> StoredLineup) {
        context.dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(transform(prefs.lineup().merged()))
        }
    }

    private fun Preferences.lineup(): StoredLineup =
        this[key]?.let { runCatching { json.decodeFromString<StoredLineup>(it) }.getOrNull() } ?: StoredLineup()

    /**
     * Appends built-in channels that are new since the lineup was saved (or on first run), and
     * drops built-in channels that were taken out of [DefaultChannels].
     */
    private fun StoredLineup.merged(): StoredLineup {
        val defaultIds = DefaultChannels.all.map { it.id }.toSet()
        val kept = channels.filter { it.custom || it.id in defaultIds }
        val known = kept.map { it.id }.toSet() + removedDefaults
        val missing = DefaultChannels.all.filter { it.id !in known }
        return if (missing.isEmpty() && kept.size == channels.size) this else copy(channels = kept + missing)
    }
}
