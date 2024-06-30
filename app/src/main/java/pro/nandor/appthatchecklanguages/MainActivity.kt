package pro.nandor.appthatchecklanguages

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import pro.nandor.appthatchecklanguages.tabs.LexemeOnScreen
import pro.nandor.appthatchecklanguages.tabs.ListOfPastWords
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

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var job: Job? by remember { mutableStateOf(null) }


    var word by remember { mutableStateOf("") }

    val tabs = viewModel.filteredWebsites.map { it.displayName } + listOf("History")

    var selectedTab by remember {
        mutableStateOf(0)
    }

    if (selectedTab >= tabs.size)
        selectedTab = 0

    Surface {
        Column() {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        }
                    )

                }
            }


            Box(modifier=Modifier.weight(1.0f)){
                for((index, website) in viewModel.filteredWebsites.withIndex()){
                    OneWebsiteTab(viewModel, website, selectedTab == index )
                }
                
                if (selectedTab == tabs.size-1){
                    ListOfPastWords(viewModel = viewModel)
                }


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
//                            navigator2.loadUrl("https://tatoeba.org/en/sentences/search?from=deu&query=$word&to=eng")
//                            navigator4.loadUrl("https://verben.org/konjugation/$word")
//                            navigator5.loadUrl("https://www.deepl.com/en/translator#de/en/$word")
                            viewModel.setWordForWebsiteSearch(word)
                        }
                    },
                    modifier = Modifier.weight(1.0f)
                )
                Button(
                    onClick = {
                        viewModel.showPopup()
                    },

                    modifier = Modifier
                        .padding(horizontal = 8.dp)

                ) {
                    Text("Done",
                        modifier = Modifier.combinedClickable (
                            onClick = {
                                viewModel.showPopup()
                            },
                            onLongClick = {
                                // todo this should not be here
                                viewModel.showLanguageSelectorDialog(true)
                            }
                        )
                    )
                }
            }
        }

        AddPopup(viewModel = viewModel, word = word)
        LanguagePickerDialog(viewModel = viewModel)
    }
}
@Composable
fun AddPopup(viewModel: MainViewModel, word: String){
    if (!viewModel.popupVisible)
        return

    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text

    val highlightedText = if (clipboardText?.contains(word, ignoreCase = true) == true && word.isNotEmpty()) clipboardText.replace(word.toRegex(RegexOption.IGNORE_CASE), {"<b>${it.value}</b>"}) else null

    var radioSelection by remember{ mutableStateOf(2) }


    var customContextSentence by remember {
        mutableStateOf("")
    }

    var customTranslation by remember {
        mutableStateOf("")
    }

    var customWord by remember {
        mutableStateOf(word)
    }


    Dialog(onDismissRequest = {viewModel.hidePopup()}){
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Column(modifier = Modifier.padding(16.dp)){
                Text("German")
                TextField(value = viewModel.source, onValueChange = {viewModel.writeSource(it)}, placeholder = {Text("Source...", modifier = Modifier.alpha(0.25f))})
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = customWord, onValueChange = {customWord = it}, placeholder = {Text("Word...", modifier = Modifier.alpha(0.25f))})
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
                    TextField(value = customContextSentence, onValueChange = {customContextSentence = it}, placeholder = {Text("Context Sentence...", modifier = Modifier.alpha(0.25f))})

                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = customTranslation, onValueChange = {customTranslation = it}, placeholder = {Text("Translation...", modifier = Modifier.alpha(0.25f))})

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {
                     viewModel.saveWord(
                         language = viewModel.selectedLanguage,
                         foreignWord = customWord,
                         englishWord = customTranslation,
                         foreignContext = when(radioSelection){
                             0 -> clipboardText
                             1 -> highlightedText
                             2 -> customContextSentence
                             else -> ""
                         }?:""

                     )
                    viewModel.hidePopup()
                }, modifier = Modifier.fillMaxWidth()){
                    Text("Save!")
                }

            }
        }
    }
}

@Composable
fun LanguagePickerDialog(viewModel: MainViewModel){
    if (!viewModel.showLanguageSelectorDialog)
        return


    Dialog(onDismissRequest = {viewModel.showLanguageSelectorDialog(false)}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()){
                items(viewModel.websites.map { it.language }.toSet().toList()){
                    TextButton(onClick = {
                        viewModel.selectLanguage(it)
                        viewModel.showLanguageSelectorDialog(false)
                    }) {
                        Text(it)

                    }
                }
            }
        }
    }
}

@Composable
fun OneWebsiteTab(viewModel: MainViewModel, website: Website, visible: Boolean){
    val state = rememberWebViewState(website.baseURL)
    val navigator = rememberWebViewNavigator()

    var lastWord by remember{mutableStateOf("")}

    val currentWord = viewModel.wordThatWebsitesShouldSearchFor

    if (currentWord != lastWord){
        lastWord = currentWord
        if (website.shouldRefreshOnWordChange)
            navigator.loadUrl(website.urlWithOptionalPlaceholder.replace("<<word>>", currentWord))

    }

    val visibleModifier = Modifier.fillMaxSize()
    val invisibleModifier = Modifier
        .height(0.dp)
        .width(0.dp)
        .alpha(0.0f)

    WebView(state = state, navigator = navigator,
        modifier = if (visible) visibleModifier else invisibleModifier,
        onCreated = { it.settings.javaScriptEnabled = true })
}