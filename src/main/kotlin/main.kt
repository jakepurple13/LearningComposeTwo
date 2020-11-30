@file:Suppress("FunctionName")

import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.net.URL
import anime.Sources as ASources
import manga.Sources as MSources

data class DialogShowing(val show: Boolean = false, val list: List<GenericInfo> = emptyList(), val title: String = "")

@ExperimentalFoundationApi
@ExperimentalKeyInput
@ExperimentalMaterialApi
fun main() = Window(title = "Otaku Viewer") {
    val theme = remember { mutableStateOf(darkColors()) }
    var checked by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(DialogShowing()) }
    if (showDialog.value.show) {
        SourceDialog(theme, showDialog)
    }
    MaterialTheme(colors = theme.value) {
        Column {
            Row(Modifier.background(theme.value.background).fillMaxWidth().padding(top = 5.dp)) {
                Spacer(Modifier.weight(8f))
                Text(
                    if (checked) "Light" else "Dark",
                    modifier = Modifier.padding(horizontal = 5.dp).align(Alignment.CenterVertically).weight(1f),
                    color = theme.value.onBackground,
                    style = MaterialTheme
                        .typography
                        .h6,
                    textAlign = TextAlign.End
                )
                Switch(
                    checked,
                    onCheckedChange = {
                        theme.value = if (it) lightColors() else darkColors()
                        checked = it
                    },
                    modifier = Modifier.padding(horizontal = 5.dp).align(Alignment.CenterVertically).weight(1f)
                )
            }
            Row(Modifier.background(theme.value.background).wrapContentSize()) {
                OutlinedButton(
                    modifier = Modifier
                        .background(theme.value.background, shape = RoundedCornerShape(5.dp))
                        .padding(5.dp)
                        .fillMaxHeight()
                        .weight(1f, true),
                    //onClick = { uiViewer(MSources.NINE_ANIME, "Manga", theme) },
                    onClick = {
                        val items = MSources.values()
                        if (items.size == 1) {
                            uiViewer(items.first(), "Manga", theme)
                        } else {
                            showDialog.value = DialogShowing(true, items.toList(), "Manga")
                        }
                    },
                    colors = ButtonConstants.defaultButtonColors(backgroundColor = theme.value.surface)
                ) { Text("Manga", style = MaterialTheme.typography.h1) }
                OutlinedButton(
                    modifier = Modifier
                        .background(theme.value.background, shape = RoundedCornerShape(5.dp))
                        .padding(5.dp)
                        .fillMaxHeight()
                        .weight(1f, true),
                    //onClick = { uiViewer(ASources.GOGOANIME, "Anime", theme) },
                    onClick = {
                        val items = ASources.values()
                        if (items.size == 1) {
                            uiViewer(items.first(), "Anime", theme)
                        } else {
                            showDialog.value = DialogShowing(true, items.toList(), "Anime")
                        }
                    },
                    colors = ButtonConstants.defaultButtonColors(backgroundColor = theme.value.surface)
                ) { Text("Anime", style = MaterialTheme.typography.h1) }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalKeyInput
@ExperimentalFoundationApi
@Composable
fun SourceDialog(theme: MutableState<Colors>, showDialog: MutableState<DialogShowing>) =
    Dialog({ showDialog.value = DialogShowing() }) {
        MaterialTheme(colors = theme.value) {
            LazyColumnFor(showDialog.value.list, modifier = Modifier.fillMaxHeight().background(theme.value.background)) {
                Card(
                    shape = RoundedCornerShape(4.dp),
                    border = cardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            uiViewer(it, showDialog.value.title, theme)
                            showDialog.value = DialogShowing()
                        }
                ) {
                    Text(
                        it.toString(),
                        style = MaterialTheme
                            .typography
                            .h6
                            .copy(textAlign = TextAlign.Center),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

@Suppress("RemoveRedundantQualifierName")
@ExperimentalFoundationApi
@ExperimentalKeyInput
@ExperimentalMaterialApi
fun uiViewer(info: GenericInfo, title: String, theme: MutableState<Colors>) = Window(
    title = "$title Viewer",
    centered = true
) {
    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    var page = 1
    var progressAlpha by remember { mutableStateOf(1f) }
    var currentList by remember { mutableStateOf(info.getItems(page).toMutableList()) }
    MaterialTheme(colors = theme.value) {
        Column(Modifier.background(theme.value.background).padding(5.dp)) {
            TextField(
                value = textValue,
                textStyle = androidx.compose.material.AmbientTextStyle.current.copy(color = theme.value.onBackground),
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                onValueChange = { textValue = it },
                trailingIcon = { Icon(Icons.Filled.Search) },
                label = { Text("${currentList.size} Search") },
                singleLine = true,
                placeholder = { Text("Search") }
            )
            CircularProgressIndicator(Modifier.alpha(progressAlpha))
            progressAlpha = 0f
            Box {
                val listState = rememberLazyListState()
                LazyColumnForIndexed(
                    currentList.filter { textValue.text in it.title },
                    state = listState,
                    modifier = Modifier.fillMaxHeight()
                ) { index, item ->
                    if (currentList.lastIndex == index && textValue.text.isEmpty()) {
                        onActive {
                            //fetch more items here
                            progressAlpha = 1f
                            currentList = currentList.apply { addAll(info.getItems(++page)) }
                            println("New items - $page - ${currentList.size}")
                            progressAlpha = 0f
                        }
                    }
                    RowItem(item, theme)
                    Divider()
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
                        itemCount = currentList.size,
                        averageItemSize = 37.dp
                    )
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun RowItem(item: GenericData, theme: MutableState<Colors>) {
    Card(
        shape = RoundedCornerShape(4.dp),
        border = cardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { GlobalScope.launch { item.listOfData?.let { InfoLayout(it, theme) } } }
    ) {
        Text(
            item.title,
            style = MaterialTheme
                .typography
                .h6
                .copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(16.dp)
        )
    }
}

const val WIDTH_DEFAULT = 360 / 2
const val HEIGHT_DEFAULT = 480 / 2

@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun InfoLayout(item: GenericInformation, theme: MutableState<Colors>) = GlobalScope.launch {
    Window(title = item.title) {
        MaterialTheme(colors = theme.value) {
            Box {
                Column(Modifier.background(theme.value.background)) {
                    TitleArea(item)
                    ItemRows(item, theme)
                }
            }
        }
    }
}

@Composable
fun TitleArea(item: GenericInformation) = Card(modifier = Modifier.padding(5.dp), border = cardBorder()) {
    Row(modifier = Modifier.padding(5.dp)) {
        Image(
            bitmap = org.jetbrains.skija.Image.makeFromEncoded(
                URL(item.imageUrl).openConnection().getInputStream().readAllBytes()
            ).asImageBitmap(),
            modifier = Modifier
                .size(WIDTH_DEFAULT.dp, HEIGHT_DEFAULT.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colors.background), shape = RoundedCornerShape(5.dp))
        )
        Column(modifier = Modifier.padding(5.dp).height(HEIGHT_DEFAULT.dp)) {
            Text(
                item.title,
                style = MaterialTheme
                    .typography
                    .h3
                    .copy(textAlign = TextAlign.Center)
            )
            Text(
                item.url,
                modifier = Modifier.clickable { Desktop.getDesktop().browse(URI.create(item.url)) },
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme
                    .typography
                    .subtitle1
                    .copy(textAlign = TextAlign.Center, color = Color.Cyan)
            )
            ScrollableRow(modifier = Modifier.padding(5.dp)) {
                item.genres.forEach { Text(it, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.subtitle2) }
            }
            ScrollableColumn { Text(item.description.orEmpty(), style = MaterialTheme.typography.body1) }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun ItemRows(item: GenericInformation, theme: MutableState<Colors>) {
    Box(modifier = Modifier.padding(5.dp)) {
        val listState = rememberLazyListState()
        val items = item.rowData()
        LazyColumnFor(items, state = listState, modifier = Modifier.fillMaxHeight()) {
            Card(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
                    .clickable { item.playUi(it.getItem(), it.name, theme) },
                border = cardBorder()
            ) {
                Column(modifier = Modifier.padding(5.dp)) {
                    Text(it.name)
                    Text(it.uploaded, modifier = Modifier.align(Alignment.End))
                }
            }
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
                itemCount = items.size,
                averageItemSize = 37.dp
            )
        )
    }
}

@Composable
fun cardBorder() = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
