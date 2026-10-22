# Lietuvos TV (Android TV)

An Android TV app for Lithuanian live TV. It starts playing the first channel as soon as it opens,
and every screen works with a D-pad remote.

## Remote controls

| Key | Action |
| --- | --- |
| Up / Channel down | Previous channel |
| Down / Channel up | Next channel |
| OK or Left | Channel list |
| Menu | Manage channels |
| Play/Pause | Pause or resume |
| Back | Close the list or manage screen |

## Channels

Built in (official broadcaster streams only): LRT televizija, LRT Plius, LRT Lituanica, Delfi TV,
Lietuvos rytas TV, LRT Radijas, LRT Klasika, LRT Opus. Several LRT streams are geo-blocked to Lithuania.

Commercial channels (LNK, TV3, BTV, TV6, TV8, TV1, Info TV) have no free official live stream, so they
are not bundled. Add any stream you have access to from **Manage channels → Add channel**.

In **Manage channels** you can hide, show, reorder and remove channels. The first channel that isn't
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
