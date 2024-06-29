package pro.nandor.appthatchecklanguages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        setContent {
            AppThatChecksLanguagesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val state = rememberWebViewState("https://example.com")
    val navigator = rememberWebViewNavigator()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var job: Job? by remember { mutableStateOf(null) }


    var word by remember { mutableStateOf("") }
    Column() {
        TextField(
            value = word,
            onValueChange = {
                word = it
                job?.cancel()
                job = coroutineScope.launch {
                    delay(1000) // Delay for 1 second
                    navigator.loadUrl("https://en.wiktionary.org/wiki/$word")
                }
            })
        WebView(state = state, navigator = navigator)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppThatChecksLanguagesTheme {
        Greeting("Android")
    }
}