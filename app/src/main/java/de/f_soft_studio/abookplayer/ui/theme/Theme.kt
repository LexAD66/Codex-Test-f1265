package de.f_soft_studio.abookplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Setzt das thematische Erscheinungsbild der Anwendung.
 *
 * Alle Kommentare sind bewusst ausführlich, um den Beweggrund jeder Entscheidung
 * festzuhalten. So können zukünftige Maintainer:innen schneller nachvollziehen,
 * weshalb beispielsweise dynamische Farben optional und nicht verpflichtend sind.
 */
@Composable
fun ABookPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        // Begründung: Durch die Synchronisierung der Systemleisten mit dem Theme wirkt die App nahtlos.
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !darkTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Begründung: Wir lassen den Inhalt hinter die Systemleisten laufen für ein immersives Erlebnis.
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Definiert das helle Farbschema, falls kein dynamisches Material verfügbar ist.
 */
@Composable
private fun lightColorScheme(): ColorScheme = androidx.compose.material3.lightColorScheme(
    primary = ABookColors.seed,
    surface = ABookColors.surfaceLight,
    background = ABookColors.surfaceLight
)

/**
 * Definiert das dunkle Farbschema.
 */
@Composable
private fun darkColorScheme(): ColorScheme = androidx.compose.material3.darkColorScheme(
    primary = ABookColors.seed,
    surface = ABookColors.surfaceDark,
    background = ABookColors.surfaceDark
)
