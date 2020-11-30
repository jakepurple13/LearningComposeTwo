package anime

import GenericUi
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

object AnimeUi : GenericUi {
    override fun playUi(list: List<String>, title: String, theme: MutableState<Colors>) = Window(title) {
        MaterialTheme(colors = theme.value) {
            Button(
                modifier = Modifier.background(theme.value.background, shape = RoundedCornerShape(5.dp)).fillMaxSize(),
                onClick = { list.firstOrNull()?.let { Desktop.getDesktop().browse(URI.create(it)) } }
            ) { Text("Play Video") }
        }

    }
}