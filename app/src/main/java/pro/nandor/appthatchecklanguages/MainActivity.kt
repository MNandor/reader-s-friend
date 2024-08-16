package pro.nandor.appthatchecklanguages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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


    val allWords by viewModel.words
    val ourwords by viewModel.wordsOfThisLang
    val recentWords by viewModel.recentWords
    val recentWordsOfThisLang by viewModel.recentWordsOfThisLang
    val unexportedWords by viewModel.unexportedWords
    val unexportedWordsOfThisLang by viewModel.unexportedWordsOfThisLang

    val switchLanguageCallback = {viewModel.showLanguageSelectorDialog(true)}
    val data = TopbarData(
        viewModel.selectedLanguage,
        recentWordsOfThisLang.size,
        recentWords.size,
        ourwords.size,
        allWords.size,
        unexportedWordsOfThisLang.size,
        unexportedWords.size
    )

    Surface {
        Column {
            TopBar(data = data, onLanguageButtonClicked = switchLanguageCallback)
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

data class TopbarData(
    val currentLanguage: String,
    val addedTodayThisLanguage: Int,
    val addedTodayAnyLanguage: Int,
    val addedEverThisLanguage: Int,
    val addedEverAnyLanguage: Int,
    val unexportedThisLanguage: Int,
    val unexportedAnyLanguage: Int
)
@Composable
fun TopBar(data: TopbarData, onLanguageButtonClicked: () -> Unit){
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        TextButton(onClick = { onLanguageButtonClicked()}) {
            Text(data.currentLanguage)
            
        }
        ClickableText(text = AnnotatedString(
            "Today: ${data.addedTodayThisLanguage}/${data.addedTodayAnyLanguage}"),
            onClick = {
                val wasWere = if (data.addedTodayThisLanguage == 1) "was" else "were"
                Toast.makeText(context, "Today, ${data.addedTodayThisLanguage} $wasWere added to ${data.currentLanguage}, and ${data.addedTodayAnyLanguage} to any language.", Toast.LENGTH_SHORT).show()
            },

        )
        ClickableText(text = AnnotatedString(
            "Total: ${data.addedEverThisLanguage}/${data.addedEverAnyLanguage}"),
            onClick = {
                val wasWere = if (data.addedEverThisLanguage == 1) "was" else "were"
                Toast.makeText(context, "${data.addedEverThisLanguage} $wasWere ever added to ${data.currentLanguage}, and ${data.addedEverAnyLanguage} to any language.", Toast.LENGTH_SHORT).show()
            },

        )

        ClickableText(text = AnnotatedString(
            "Todo: ${data.unexportedThisLanguage}/${data.unexportedAnyLanguage}"),
            onClick = {
                val wasWere = if (data.unexportedThisLanguage == 1) "was" else "were"
                Toast.makeText(context, "${data.unexportedThisLanguage} $wasWere not exported from ${data.currentLanguage}, and ${data.unexportedAnyLanguage} from any language.", Toast.LENGTH_SHORT).show()
            },

            )
    }
}

@Preview
@Composable
fun SampleTopBar(){
    val data = TopbarData("German", 10, 15, 100, 150, 100, 150)

    TopBar(data, {})
}