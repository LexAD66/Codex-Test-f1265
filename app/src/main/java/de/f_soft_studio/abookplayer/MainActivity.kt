package de.f_soft_studio.abookplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint
import de.f_soft_studio.abookplayer.ui.navigation.ABookNavGraph
import de.f_soft_studio.abookplayer.ui.theme.ABookPlayerTheme
import de.f_soft_studio.abookplayer.R
import kotlinx.coroutines.launch

/**
 * Host-Aktivität für die Compose-Navigation. Wir kümmern uns hier um
 * Berechtigungen für lokale Audiodateien.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ABookPlayerTheme {
                PermissionGate()
            }
        }
    }
}

@Composable
private fun PermissionGate() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            scope.launch {
                Toast.makeText(context, context.getString(R.string.permission_read_media_audio), Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(permission)
        }
    }
    ABookNavGraph()
}
