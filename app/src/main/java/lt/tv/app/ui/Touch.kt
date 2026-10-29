package lt.tv.app.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * TV Material components only react to the remote's OK button, not to touch, so phones need this.
 * It adds no focus target, so D-pad navigation on the TV is unchanged.
 */
fun Modifier.onTap(action: () -> Unit): Modifier = pointerInput(action) { detectTapGestures { action() } }
