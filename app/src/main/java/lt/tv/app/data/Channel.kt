package lt.tv.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val hidden: Boolean = false,
    /** True for channels the user added; false for the built-in list. */
    val custom: Boolean = false,
    /** Some streams only answer to a browser-like User-Agent. */
    val userAgent: String? = null,
)
