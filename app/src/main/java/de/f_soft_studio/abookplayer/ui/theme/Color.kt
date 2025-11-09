package de.f_soft_studio.abookplayer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Stellt zentrale Farbwerte für das Design zur Verfügung.
 * Die Definitionen orientieren sich am Material-3-Schema, um das adaptive
 * Farbsystem zur Laufzeit generieren zu können.
 */
object ABookColors {
    /** Primärer Saatwert, aus dem das dynamische Schema generiert wird. */
    val seed = Color(0xFF6750A4)
    /** Hellgrauer Hintergrund für helle Oberflächen. */
    val surfaceLight = Color(0xFFFFFBFE)
    /** Dunkler Hintergrund für Nachtmodus. */
    val surfaceDark = Color(0xFF1C1B1F)
}
