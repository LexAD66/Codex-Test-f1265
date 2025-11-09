# ABook Player

Dieses Repository enthält ein vollständiges Android-Studio-Projekt für den "ABook Player". Die App richtet sich an Nutzer:innen, die lokale Hörbücher verwalten und abspielen möchten. Sie basiert auf Jetpack Compose, Material 3, Hilt, Room und Media3.

## Highlights
- MediaSessionService (`AbookPlaybackService`) mit ExoPlayer, Sleep-Timer, Geschwindigkeit und Benachrichtigungskanal `abook_playback`.
- Bibliotheksscan des Standardverzeichnisses `/storage/emulated/0/ABook/` inklusive `.abook`-Containern.
- Room-Datenbank mit Büchern, Kapiteln, Lesezeichen und gespeicherten Positionen.
- Drei Hauptbildschirme: Bibliothek, Player, Details. UI-Texte vollständig in Deutsch.
- Deeplinks für `https://f-soft-studio.de/abook/*` vorbereitet.
- Unit- und Instrumentation-Test für Kernpfade.

## Projektstruktur
```
app/
 ├── src/main/java/de/f_soft_studio/abookplayer
 │    ├── data/…        # Room-Entities, DAOs, Repositories
 │    ├── domain/…      # Use-Cases
 │    ├── playback/…    # MediaSessionService und Player-Manager
 │    ├── ui/…          # Compose Screens, ViewModels, Navigation
 │    └── MainActivity.kt
 └── build.gradle.kts   # Modul-Konfiguration
```

## Entwicklung
1. Öffne das Projekt in Android Studio Narwhal 4 – Feature Drop (2025.1.4).
2. Sync mit Gradle und stelle sicher, dass das Android SDK (min. API 26, target 34) installiert ist.
3. Starte die App auf einem Gerät oder Emulator (empfohlen ist ein Gerät mit lokaler Hörbuchbibliothek).
4. Erlaube der App den Zugriff auf lokale Audiodateien, damit der Scanner alle Titel findet.

## Tests
- Unit-Tests: `./gradlew test`
- Instrumentation-Smoke-Test: `./gradlew connectedAndroidTest`

## Lizenz
Siehe Projektdokumentation in `docs/` für Status und nächste Schritte.
