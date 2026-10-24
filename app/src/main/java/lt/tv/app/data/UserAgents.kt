package lt.tv.app.data

/** Common User-Agents offered when adding or editing a channel. Some streams only answer to a browser. */
object UserAgents {
    const val CHROME =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"

    /** Label to User-Agent. */
    val presets: List<Pair<String, String>> = listOf(
        "Chrome (Windows)" to CHROME,
        "Firefox (Windows)" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:150.0) Gecko/20100101 Firefox/150.0",
        "Safari (Mac)" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/19.0 Safari/605.1.15",
        "VLC" to "VLC/3.0.21 LibVLC/3.0.21",
    )
}
