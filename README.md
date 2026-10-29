# Lietuvos TV (Android TV)

An Android TV app for Lithuanian live TV. It starts playing the first channel as soon as it opens,
and every screen works with a D-pad remote.

# Verified Working
- Nvidia Shield
- Amazon Fire Stick - 4k Max (1st Gen) running Fire OS 7.7.1.3.
- Samsung Galaxy Ultra S25

## Remote controls

| Key | Action |
| --- | --- |
| Up / Channel down / swipe down | Previous channel |
| Down / Channel up / swipe up | Next channel |
| OK or Left / tap | Channel list |
| Menu | Manage channels |
| Play/Pause | Pause or resume |
| Back | Close the list or manage screen |

## Channels

Built in: LRT Lituanica, LRT Plius, TV3, Delfi TV, Lietuvos rytas TV, Go3 Sport1, LRT Radijas, LRT Klasika,
LRT Opus, Pūko radijas. TV3, LRT Plius and Go3 Sport1 are third-party restreams. LRT streams may be geo-blocked to
Lithuania. Add any other stream from **Manage channels → Add channel**.

In **Manage channels** you can edit, hide, show, reorder and remove channels. The first channel that isn't
hidden plays on startup. **Restore defaults** brings back the built-in list and keeps your own channels.

## Phone companion (planned)

All playback goes through `control/ControlBus`: inputs send `PlayerCommand`s and the player publishes
`PlaybackStatus`. The phone app will talk to a small LAN server in the TV app that forwards to the same
bus, so no player code needs to change.

## Build

Requires Android Studio (or the Android SDK with platform 35) and JDK 17+.

```
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```
