package pro.nandor.appthatchecklanguages.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pro.nandor.appthatchecklanguages.Lexeme
import pro.nandor.appthatchecklanguages.MainViewModel
import pro.nandor.appthatchecklanguages.TextThatHighlights

@Composable
fun ListOfPastWords(viewModel: MainViewModel){
    val words by viewModel.wordsOfThisLang

    val callBack:(Lexeme) -> Unit = {it:Lexeme ->
        viewModel.setLexemeForDeletion(it)
    }

    Surface {
        LazyColumn(modifier = Modifier.fillMaxSize()){
            items(words){
                LexemeOnScreen(lexeme = it, callBack)
            }
        }
        ConfirmLexemeDeletion(viewModel = viewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LexemeOnScreen(lexeme: Lexeme, callBack: (Lexeme) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {

                },
                onLongClick = {
                    callBack(lexeme)
                }
            )
    ){
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)){
            Text(lexeme.language, fontSize = 8.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
                Text(lexeme.englishWord)
                Text(lexeme.foreignWord)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextThatHighlights(lexeme.foreignContext)
        }
    }

}

@Preview
@Composable
fun PreviewLexeme(){
    val lexeme = Lexeme().apply {
        this.englishWord = "dog"
        this.foreignWord = "der Hund, die Hunde"
        this.foreignContext = "Der Hund lauft schnell!"
        this.language = "German"
        this.exportTimeStamp = 0
    }

    LexemeOnScreen(lexeme = lexeme, {})
}

@Composable
fun ConfirmLexemeDeletion(viewModel: MainViewModel){

    if (viewModel.lexemeToBeDeleted == null)
        return

    Dialog(onDismissRequest = {
        viewModel.setLexemeForDeletion(null)
    }
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            Column {
                Text("Really delete this?")
                viewModel.lexemeToBeDeleted?.let {
                    LexemeOnScreen(lexeme = it, callBack = {})
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween){
                    TextButton(onClick = {
                        viewModel.setLexemeForDeletion(null)
                    }) {
                        Text("no")
                    }
                    TextButton(onClick = {
                        viewModel.lexemeToBeDeleted?.let {  viewModel.deleteLexeme(it) }
                        viewModel.setLexemeForDeletion(null)

                    }) {
                        Text("Yes")
                    }

                }

            }
        }

    }
}