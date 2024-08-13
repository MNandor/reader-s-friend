package pro.nandor.appthatchecklanguages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddPopup(viewModel: MainViewModel, word: String){
    if (!viewModel.popupVisible)
        return

    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text

    val highlightedText = clipboardText?.let { Util.suggestHighlightedWord(it, word) }

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
                Text(viewModel.selectedLanguage)
                TextField(value = viewModel.source, onValueChange = {viewModel.writeSource(it)}, placeholder = { Text("Source...", modifier = Modifier.alpha(0.25f)) })
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ){
                    if (viewModel.showSourceCrementButton){
                        Row(){
                            Button(onClick = { /*TODO*/ }) {
                                Text("-")
                            }
                            Button(onClick = { /*TODO*/ }) {
                                Text("+")
                            }

                        }
                    }
                    if (viewModel.showSourceAsteriskToggle){
                        Row(){
                            Checkbox(checked = viewModel.source.endsWith("*"), onCheckedChange = {
                                if (viewModel.source.endsWith("*"))
                                    viewModel.writeSource(viewModel.source.dropLast(1))
                                else
                                    viewModel.writeSource(viewModel.source+"*")
                            })
                            Text("Asterisk")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = customWord, onValueChange = {customWord = it}, placeholder = { Text("Word...", modifier = Modifier.alpha(0.25f)) })
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
                        TextThatHighlights(highlightedText)
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(selected = radioSelection == 2, onClick = { radioSelection = 2 })
                    TextField(value = customContextSentence, onValueChange = {customContextSentence = it}, placeholder = { Text("Context Sentence...", modifier = Modifier.alpha(0.25f)) })

                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = customTranslation, onValueChange = {customTranslation = it}, placeholder = { Text("Translation...", modifier = Modifier.alpha(0.25f)) })

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
                        }?:"",
                        source = viewModel.source

                    )
                    viewModel.hidePopup()
                }, modifier = Modifier.fillMaxWidth()){
                    Text("Save!")
                }

            }
        }
    }
}