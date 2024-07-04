package pro.nandor.appthatchecklanguages

object Util {
    private val tag = object {
        val left: String = "<b>"
        val right: String = "</b>"
    }
    fun suggestHighlightedWord(_sentence: String, _word: String):String?{

        if (_word.isEmpty() || _sentence.isEmpty())
            return null

        val sentence = _sentence.lowercase()
        val word = _word.lowercase()

        val regex = Regex(pattern = "\\b$word\\b", RegexOption.IGNORE_CASE)

        val matched = regex.find(sentence)

        if (matched != null){
            // we preserve the capitalization from the original sentence
            // not from the lowercased sentence
            // and also not of the search word
            val wordInSentence = _sentence.substring(matched.range)
            val newSentence = _sentence.replaceRange(matched.range, "${tag.left}$wordInSentence${tag.right}")
            return newSentence
        }

        if (word.length < 3){
            // word is too short for non-exact matches to be significant
            return null
        }

        // allow suffixes and prefixes
        val lenientRegex = Regex(pattern = "\\b\\w*$word\\w*\\b", RegexOption.IGNORE_CASE)

        val lenientMatched = lenientRegex.find(sentence)

        if (lenientMatched != null) {
            val wordInSentence = _sentence.substring(lenientMatched.range)
            val newSentence = _sentence.replaceRange(lenientMatched.range, "${tag.left}$wordInSentence${tag.right}")
            return newSentence
        }


        return null
    }
}