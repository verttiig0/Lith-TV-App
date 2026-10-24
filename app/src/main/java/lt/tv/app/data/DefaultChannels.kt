package lt.tv.app.data

/**
 * Built-in channels. TV3, LRT Plius and Go3 Sport1 are third-party restreams (mcquack.net), not
 * official broadcaster streams; they only answer to a browser User-Agent. Other channels can be added from the Manage screen.
 *
 * LRT streams may be geo-blocked to Lithuania.
 */
object DefaultChannels {
    private const val BROWSER_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"

    val all: List<Channel> = listOf(
        Channel(id = "tv3", name = "TV3", url = "http://stream.mcquack.net/29/index.m3u8", userAgent = BROWSER_UA),
        // New id so installs with the old geo-blocked LRT Plius stream pick up this one.
        Channel(id = "lrt-plius-mq", name = "LRT Plius", url = "http://stream.mcquack.net/353/index.m3u8", userAgent = BROWSER_UA),
        Channel(id = "lrt-lituanica", name = "LRT Lituanica", url = "https://stream-live.lrt.lt/lituanica/master.m3u8"),
        Channel(id = "delfi-tv", name = "Delfi TV", url = "https://s1.dcdn.lt/live/televizija/playlist.m3u8"),
        Channel(id = "lryto-tv", name = "Lietuvos rytas TV", url = "https://live.lietuvosryto.tv/live/hls/eteris.m3u8"),
        Channel(id = "go3-sport1", name = "Go3 Sport1", url = "http://stream.mcquack.net/416/index.m3u8", userAgent = BROWSER_UA),
        Channel(id = "lrt-radijas", name = "LRT Radijas", url = "https://stream-live.lrt.lt/radijas/master.m3u8"),
        Channel(id = "lrt-klasika", name = "LRT Klasika", url = "https://stream-live.lrt.lt/klasika/master.m3u8"),
        Channel(id = "lrt-opus", name = "LRT Opus", url = "https://stream-live.lrt.lt/opus/master.m3u8"),
        Channel(id = "puko-radijas", name = "Pūko radijas", url = "http://212.95.45.82:8000/stream"),
    )
}
