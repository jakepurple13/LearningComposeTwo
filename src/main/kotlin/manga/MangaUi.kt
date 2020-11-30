package manga

import GenericUi
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skija.Image
import java.net.URL

@ExperimentalFoundationApi
fun main() {
    MangaUi.playUi(
        listOf(
            "https://picsum.photos/200/300",
            "https://picsum.photos/200/300",
            "https://picsum.photos/200/300",
            "https://picsum.photos/200/300",
            "https://picsum.photos/200/300"
        ),
        "alksdjfh",
        mutableStateOf(darkColors())
    )
}

object MangaUi : GenericUi {
    @ExperimentalFoundationApi
    override fun playUi(list: List<String>, title: String, theme: MutableState<Colors>) = Window(title = title) {
        var alpha by remember { mutableStateOf(1f) }
        var pageList by remember { mutableStateOf(emptyList<ImageBitmap>()) }
        MaterialTheme(colors = theme.value) {
            Box(modifier = Modifier.background(theme.value.background).padding(5.dp).fillMaxSize()) {
                val listState = rememberLazyListState()
                CircularProgressIndicator(Modifier.alpha(alpha).align(Alignment.Center))
                LazyColumnFor(
                    pageList,
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp)
                        .fillMaxWidth()
                        .background(theme.value.background)
                ) {
                    Image(bitmap = it, modifier = Modifier.padding(vertical = 3.dp).border(1.dp, theme.value.onBackground))
                    println(it)
                }
                VerticalScrollbar(
                    style = ScrollbarStyleAmbient.current.copy(
                        hoverColor = theme.value.onBackground,
                        unhoverColor = theme.value.onBackground.copy(alpha = 0.5f),
                        hoverDurationMillis = 250
                    ),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = listState,
                        itemCount = pageList.size,
                        averageItemSize = 100.dp
                    )
                )
            }
        }

        GlobalScope.launch {
            val pages = withContext(Dispatchers.Default) {
                list
                    .also { println(it) }
                    .map { Image.makeFromEncoded(URL(it).openConnection().getInputStream().readAllBytes()).asImageBitmap() }
            }
            pageList = pages
            alpha = 0f
        }
    }
}

