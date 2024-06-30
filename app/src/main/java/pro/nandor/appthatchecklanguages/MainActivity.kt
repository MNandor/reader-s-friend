package pro.nandor.appthatchecklanguages

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.nandor.appthatchecklanguages.ui.theme.AppThatChecksLanguagesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel = MainViewModel()

        setContent {
            AppThatChecksLanguagesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android", viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(name: String, viewModel: MainViewModel) {
    val state = rememberWebViewState("https://example.com")
    val state2 = rememberWebViewState("https://example.com")
    val state3 = rememberWebViewState("https://der-artikel.de/")
    val navigator = rememberWebViewNavigator()
    val navigator2 = rememberWebViewNavigator()
    val navigator3 = rememberWebViewNavigator()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var job: Job? by remember { mutableStateOf(null) }


    var word by remember { mutableStateOf("") }

    val tabs = listOf("Wiktionary", "Tatoeba", "Artikel")

    var selectedTab by remember {
        mutableStateOf(0)
    }

    Surface {
        Column() {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        }
                    )

                }
            }

            val visibleModifier = Modifier.fillMaxSize()
            val invisibleModifier = Modifier
                .height(0.dp)
                .width(0.dp)
                .alpha(0.0f)

            Box(modifier=Modifier.weight(1.0f)){
                    WebView(state = state, navigator = navigator,
                        modifier = if (selectedTab == 0) visibleModifier else visibleModifier)
                    WebView(state = state2, navigator = navigator2, onCreated = { it.settings.javaScriptEnabled = true },
                        modifier = if (selectedTab == 1) visibleModifier else invisibleModifier)
                    WebView(state = state3, navigator = navigator3, onCreated = { it.settings.javaScriptEnabled = true },
                        modifier = if (selectedTab == 2) visibleModifier else invisibleModifier)


            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                TextField(
                    value = word,
                    onValueChange = {
                        word = it
                        job?.cancel()
                        job = coroutineScope.launch {
                            delay(1000) // Delay for 1 second
                            navigator.loadUrl("https://en.wiktionary.org/wiki/$word")
                            navigator2.loadUrl("https://tatoeba.org/en/sentences/search?from=&query=$word&to=")
                        }
                    },
                    modifier = Modifier.weight(1.0f)
                )
                Button(
                    onClick = {
                        viewModel.showPopup()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)

                ) {
                    Text("Done")
                }
            }
        }

        AddPopup(viewModel = viewModel, word = word)
    }
}
@Composable
fun AddPopup(viewModel: MainViewModel, word: String){
    if (!viewModel.popupVisible)
        return

    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text

    val highlightedText = if (clipboardText?.contains(word, ignoreCase = true) == true) clipboardText.replace(word.toRegex(RegexOption.IGNORE_CASE), {"<b>${it.value}</b>"}) else null

    var radioSelection by remember{ mutableStateOf(0) }


    Dialog(onDismissRequest = {viewModel.hidePopup()}){
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Column(modifier = Modifier.padding(16.dp)){
                Text("German")
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = "hi", onValueChange = {})
                Spacer(modifier = Modifier.height(16.dp))
                if (clipboardText != null)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(selected = radioSelection == 0, onClick = { radioSelection = 0 })
                        Text(clipboardText)
                    }
                if (highlightedText != null)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        RadioButton(selected = radioSelection == 1, onClick = { radioSelection = 1 })
                        Text(highlightedText)
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(selected = radioSelection == 2, onClick = { radioSelection = 2 })
                    TextField(value = "jumps over", onValueChange = {})

                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = "translation", onValueChange = {})

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()){
                    Text("Save!")
                }

            }
        }
    }
}