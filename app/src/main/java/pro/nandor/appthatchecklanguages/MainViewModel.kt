package pro.nandor.appthatchecklanguages

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    val words = realm
        .query<Lexeme>()
        .asFlow()
        .map { results ->
            results.list.toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
        )

    var source by mutableStateOf("")
        private set

    fun writeSource(source: String){
        this.source = source
    }

    fun deleteLexeme(lexeme: Lexeme){
        viewModelScope.launch {
            realm.write {
                val lexemeToDelete = findLatest(lexeme)
                if (lexemeToDelete != null)
                    delete(lexemeToDelete)
            }
        }
    }

    fun setLexemeForDeletion(lexeme: Lexeme?){
        lexemeToBeDeleted = lexeme
    }

    var lexemeToBeDeleted:Lexeme? by mutableStateOf(null)
        private set

}