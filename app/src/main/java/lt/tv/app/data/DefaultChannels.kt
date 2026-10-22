package lt.tv.app.data

/**
 * Built-in channels. Only streams published by the broadcasters themselves are listed here.
 * Commercial channels (LNK, TV3, BTV, TV6, TV8, ...) have no free official live stream, so they
 * are not bundled; users can add a stream they have access to from the Manage screen.
 *
 * Several LRT streams are geo-blocked to Lithuania.
 */
object DefaultChannels {
    val all: List<Channel> = listOf(
        Channel(
            id = "lrt-tv",
            name = "LRT televizija",
            url = "https://stream-syncwords.lrt.lt/out/v1/channel-group-lrt-portal-prod-01/channel-lrt-portal-prod-01/endpoint-lrt-portal-prod-01/index.m3u8",
        ),
        Channel(id = "lrt-plius", name = "LRT Plius", url = "https://stream-live.lrt.lt/plius/master.m3u8"),
        Channel(id = "lrt-lituanica", name = "LRT Lituanica", url = "https://stream-live.lrt.lt/lituanica/master.m3u8"),
        Channel(id = "delfi-tv", name = "Delfi TV", url = "https://s1.dcdn.lt/live/televizija/playlist.m3u8"),
        Channel(id = "lryto-tv", name = "Lietuvos rytas TV", url = "https://live.lietuvosryto.tv/live/hls/eteris.m3u8"),
        Channel(id = "lrt-radijas", name = "LRT Radijas", url = "https://stream-live.lrt.lt/radijas/master.m3u8"),
        Channel(id = "lrt-klasika", name = "LRT Klasika", url = "https://stream-live.lrt.lt/klasika/master.m3u8"),
        Channel(id = "lrt-opus", name = "LRT Opus", url = "https://stream-live.lrt.lt/opus/master.m3u8"),
    )
}
