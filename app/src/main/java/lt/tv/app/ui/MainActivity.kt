package lt.tv.app.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

class MainActivity : ComponentActivity() {

    private val viewModel: TvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                App(viewModel)
            }
        }
    }
}

@Composable
private fun App(viewModel: TvViewModel) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != Screen.Player) { viewModel.show(Screen.Player) }

    Box(Modifier.fillMaxSize()) {
        // The player stays mounted under the overlays so playback continues while browsing.
        PlayerScreen(viewModel, acceptKeys = screen == Screen.Player)
        when (screen) {
            Screen.Player -> Unit
            Screen.ChannelList -> ChannelListOverlay(viewModel)
            Screen.Manage -> ManageScreen(viewModel)
        }
    }
}
