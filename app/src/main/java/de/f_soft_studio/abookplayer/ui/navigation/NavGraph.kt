package de.f_soft_studio.abookplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.f_soft_studio.abookplayer.ui.details.DetailsRoute
import de.f_soft_studio.abookplayer.ui.library.LibraryRoute
import de.f_soft_studio.abookplayer.ui.player.PlayerRoute

/**
 * Zentrales Navigations-Setup.
 */
@Composable
fun ABookNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "library",
        modifier = modifier
    ) {
        composable("library") {
            LibraryRoute(onBookSelected = { bookId ->
                navController.navigate("details/$bookId")
            })
        }
        composable(
            route = "details/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            DetailsRoute(
                onPlayChapter = { chapterId ->
                    navController.navigate("player/$chapterId")
                }
            )
        }
        composable(
            route = "player/{chapterId}",
            arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
        ) {
            PlayerRoute()
        }
    }
}
