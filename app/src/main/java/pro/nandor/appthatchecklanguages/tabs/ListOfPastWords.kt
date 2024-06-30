package pro.nandor.appthatchecklanguages.tabs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pro.nandor.appthatchecklanguages.MainViewModel

@Composable
fun ListOfPastWords(viewModel: MainViewModel){
    val words by viewModel.words.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()){
        items(words){
            Text("${it.foreignWord} - ${it.englishWord}")
        }
    }
}