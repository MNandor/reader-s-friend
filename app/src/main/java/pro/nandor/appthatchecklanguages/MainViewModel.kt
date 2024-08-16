package pro.nandor.appthatchecklanguages

import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

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
        foreignContext: String,
        source: String
    ){
        val lexeme = Lexeme().apply {
            this.language = language
            this.foreignWord = foreignWord
            this.englishWord = englishWord
            this.foreignContext = foreignContext
            this.source = source
        }

        addWordToRealm(lexeme)

    }

    private val _words = realm
        .query<Lexeme>()
        .asFlow()
        .map { results ->
            results.list.toList()
        }
        .onEach {
            // todo here we convert from Flow to State
            // this is mostly because I found it easy to work with derivedStateOf
            // but this is probably not best practice
            words.value = it
        }.launchIn(viewModelScope)
    val words = mutableStateOf(listOf<Lexeme>())

    val wordsOfThisLang = derivedStateOf{
        words.value.filter { it.language == selectedLanguage }
    }
    private val SECONDS_IN_DAY = 24*60*60

    val recentWordsOfThisLang = derivedStateOf {
        val now = System.currentTimeMillis()/1000
        wordsOfThisLang.value.filter {
            (now - it.id.timestamp) < SECONDS_IN_DAY
        }
    }

    val recentWords = derivedStateOf {

        val now = System.currentTimeMillis()/1000
        words.value.filter {
            (now - it.id.timestamp) < SECONDS_IN_DAY
        }
    }

    val unexportedWords = derivedStateOf {
        words.value.filter {
            it.exportTimeStamp == 0
        }
    }

    val unexportedWordsOfThisLang = derivedStateOf {
        wordsOfThisLang.value.filter {
            it.exportTimeStamp == 0
        }
    }

    var source by mutableStateOf("")
        private set

    fun writeSource(source: String){
        this.source = source

        if (source.endsWith("*"))
            showSourceAsteriskToggle = true

        showSourceCrementButton = Util.doesSourceContainCrementable(source)

    }

    var showSourceCrementButton: Boolean by mutableStateOf(false)
    var showSourceAsteriskToggle: Boolean by mutableStateOf(false)

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
        Website("Tatoeba", "Romanian", "https://tatoeba.org/en/sentences/search?from=ron&query=<<word>>&to=eng", true, "https://tatoeba.org/en/sentences/search?from=ron&query=&to=eng"),
        Website("Dexonline", "Romanian", "https://dexonline.ro/definitie/<<word>>", true, "https://dexonline.ro/"),

        Website("Glosbe", "Swedish", "https://en.glosbe.com/sv/en/<<word>>", true, "https://en.glosbe.com"),
        Website("Tatoeba", "Swedish", "https://tatoeba.org/en/sentences/search?from=swe&query=<<word>>&to=eng", true, "https://tatoeba.org/en/sentences/search?from=swe&query=&to=eng"),
        Website("Svenska", "Swedish", "https://svenska.se/tre/?sok=<<word>>", true, "https://svenska.se/"),
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

    enum class DialogShown {
        NONE, CLOZE, CSV, CLOZEPLUS
    }

    var dialogShown by mutableStateOf(DialogShown.NONE)
        private set

    fun showDialog(dialogToShow: DialogShown){
        dialogShown = dialogToShow
    }

    fun exportText(){
        viewModelScope.launch {

            val now = (System.currentTimeMillis()/1000).toInt()

            val unexportedWords = wordsOfThisLang.value.filter {
                it.exportTimeStamp == 0
            }

            val textToShow = when (dialogShown){
                DialogShown.CLOZE -> Util.exportAllLines(unexportedWords, Util.ExportFormat.Cloze)
                DialogShown.CSV -> Util.exportAllLines(unexportedWords, Util.ExportFormat.CSV("\t"))
                DialogShown.CLOZEPLUS -> Util.exportAllLines(unexportedWords, Util.ExportFormat.ClozeTranslationSource())
                else -> return@launch
            }

            val fileName = "rf-$selectedLanguage-${now}.txt"


            val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val file= File(targetDir.absolutePath+"/"+fileName)

            file.writeText(textToShow)

            realm.write {
                for (lexeme in unexportedWords){
                    val editableLexeme = findLatest(lexeme)
                    editableLexeme?.exportTimeStamp = now
                }

            }

        }

    }


    fun crementSourceNumber(goDown: Boolean){
        source = Util.bumpCrementable(source, goDown)
    }
}