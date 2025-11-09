package de.f_soft_studio.abookplayer

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Einfacher Smoke-Test, der sicherstellt, dass die Bibliothek dargestellt wird
 * und der Player erreichbar ist.
 */
@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun libraryScreenIsVisible() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.screen_library_title)).assertExists()
    }
}
