package pro.nandor.appthatchecklanguages

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
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

    var showLanguageSelectorDialog by mutableStateOf(false)
        private set

    fun showLanguageSelectorDialog(shouldShow: Boolean){
        showLanguageSelectorDialog = shouldShow
    }

    val websites = listOf<Website>(
        Website("Wiktionary", "German", "https://en.wiktionary.org/wiki/<<word>>#German", true, "https://en.wiktionary.org/wiki/Wiktionary:Main_Page"),
        Website("Tatoeba", "German", "https://tatoeba.org/en/sentences/search?from=deu&query=<<word>>&to=eng", true, "https://tatoeba.org/"),
        Website("Artikel", "German", "https://der-artikel.de/", false, "https://der-artikel.de/"),
        Website("Verben", "German", "https://verben.org/konjugation/<<word>>", true, "https://verben.org/"),
        Website("Deepl", "German", "https://www.deepl.com/en/translator#de/en/<<word>>", true, "https://www.deepl.com/en/translator#de/en/"),

        Website("Wiktionary", "Romanian", "https://en.wiktionary.org/wiki/<<word>>#Romanian", true, "https://en.wiktionary.org/wiki/Wiktionary:Main_Page"),
        Website("Tatoeba", "Romanian", "https://tatoeba.org/en/sentences/search?from=ron&query=<<word>>&to=eng", true, "https://tatoeba.org/en/sentences/search?from=ron&query=&to=eng")
    )

    var selectedLanguage by mutableStateOf("German")
        private set

    fun selectLanguage(language: String){
        selectedLanguage = language
    }

    val filteredWebsites by derivedStateOf {
        websites.filter { it.language == selectedLanguage }
    }

    var wordThatWebsitesShouldSearchFor by mutableStateOf("")
        private set

    fun setWordForWebsiteSearch(word: String){
        wordThatWebsitesShouldSearchFor = word
    }

}