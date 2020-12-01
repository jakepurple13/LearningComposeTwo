import androidx.compose.desktop.AppWindowAmbient
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.File
import javax.swing.JFrame

val testTheme = mutableStateOf(darkColors())
val testChecked = mutableStateOf(false)

fun main() = Window {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    MaterialTheme(colors = testTheme.value) {
        ModalDrawerLayout(
            drawerState = drawerState,
            drawerContent = {
                Column(Modifier.background(testTheme.value.background).wrapContentWidth().padding(5.dp)) {
                    ThemeSettingRow(testTheme, testChecked)
                    Divider()
                }
            }
        ) {
            Column(Modifier.background(testTheme.value.background).fillMaxSize().padding(5.dp)) {
                Image(
                    imageVector = Icons.Outlined.Settings,
                    modifier = Modifier.clickable { drawerState.open() },
                    colorFilter = ColorFilter.tint(testTheme.value.onBackground)
                )
                LazyGridFor(
                    listOf("hello", "world", "hello", "world", "hello", "world", "hello", "world", "hello", "world"),
                    3,
                    modifier = Modifier.padding(5.dp)
                ) {
                    Image(
                        bitmap = imageFromFile(File("/Users/jrein/Desktop/338-360x480.jpg")),
                        modifier = Modifier
                            .size(WIDTH_DEFAULT.dp, HEIGHT_DEFAULT.dp)
                            .align(Alignment.Center)
                        //.border(BorderStroke(1.dp, theme.value.background), shape = RoundedCornerShape(5.dp))
                    )
                    Text(
                        it,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(WIDTH_DEFAULT.dp)
                            .height(HEIGHT_DEFAULT.dp)
                            .background(
                                VerticalGradient(
                                    listOf(Color(0xaa000000), Color(0x00000000), Color(0x00ffffff)),
                                    HEIGHT_DEFAULT.toFloat(), 0f
                                )
                            ),
                        style = MaterialTheme
                            .typography
                            .h3
                            .copy(textAlign = TextAlign.Center, color = testTheme.value.onBackground)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerLayout(
    theme: MutableState<Colors>,
    checked: MutableState<Boolean>,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    otherSettings: @Composable ColumnScope.() -> Unit = {},
    bodyContent: @Composable () -> Unit
) = ModalDrawerLayout(
    drawerState = drawerState,
    drawerContent = {
        Column(Modifier.background(theme.value.background).wrapContentWidth().padding(5.dp)) {
            ThemeSettingRow(theme, checked)
            Divider()
            otherSettings()
        }
    },
    bodyContent = bodyContent
)

@Composable
fun ThemeSettingRow(theme: MutableState<Colors>, checked: MutableState<Boolean>) =
    Row(Modifier.background(theme.value.background).fillMaxWidth().padding(5.dp)) {
        Spacer(Modifier.weight(8f))
        Text(
            if (checked.value) "Light" else "Dark",
            modifier = Modifier.padding(horizontal = 5.dp).align(Alignment.CenterVertically).weight(1f),
            color = theme.value.onBackground,
            style = MaterialTheme
                .typography
                .h6,
            textAlign = TextAlign.End
        )
        Switch(
            checked.value,
            onCheckedChange = {
                theme.value = if (it) lightColors() else darkColors()
                checked.value = it
            },
            modifier = Modifier.padding(horizontal = 5.dp).align(Alignment.CenterVertically).weight(1f)
        )
    }

@Composable
fun <T> LazyGridFor(
    items: List<T>,
    rowSize: Int = 1,
    modifier: Modifier = Modifier,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val rows = items.chunked(rowSize)
    LazyColumnFor(rows) { row ->
        Row(Modifier.fillParentMaxWidth()) {
            for ((index, item) in row.withIndex()) {
                Box(Modifier.fillMaxWidth(1f / (rowSize - index)).then(modifier)) {
                    itemContent(item)
                }
            }
        }
    }
}

fun imageFromFile(file: File): ImageBitmap = org.jetbrains.skija.Image.makeFromEncoded(file.readBytes()).asImageBitmap()