package pro.nandor.appthatchecklanguages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    var popupVisible by mutableStateOf(false)
        private set

    fun showPopup(){
        popupVisible = true
    }

    fun hidePopup(){
        popupVisible = false
    }

    private val realm = MainApplication.realm

    private fun addWordToRealm(lexeme: Lexeme){
        viewModelScope.launch {
            realm.write {
                copyToRealm(lexeme)
            }
        }
    }

    fun saveWord(
        language: String,
        foreignWord: String,
        englishWord: String,
        foreignContext: String
    ){
        val lexeme = Lexeme().apply {
            this.language = language
            this.foreignWord = foreignWord
            this.englishWord = englishWord
            this.foreignContext = foreignContext
        }

        addWordToRealm(lexeme)

    }
}