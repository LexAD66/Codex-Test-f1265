# Bewahrt Klassen von Media3, damit die Wiedergabe im Release-Build stabil bleibt.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room generiert zur Laufzeit Schema-Klassen; verhindern des Shrinkens.
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Hilt generiert Komponenten, die nicht entfernt werden dürfen.
-keep class dagger.hilt.internal.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-dontwarn dagger.hilt.internal.**
