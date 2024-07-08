package pro.nandor.appthatchecklanguages

import java.lang.StringBuilder

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

    sealed interface ExportFormat{

        object Cloze: ExportFormat
        data class CSV(val separator: String = "\t"): ExportFormat
    }
    fun toExportableLine(lexeme: Lexeme, exportFormat:ExportFormat = ExportFormat.Cloze):String{

        when(exportFormat){
            is ExportFormat.Cloze -> {
                val baseSentence = lexeme.foreignContext
                val exportable = baseSentence.replace(tag.left, "{{c1::").replace(tag.right, "}}")

                return exportable
            }
            is ExportFormat.CSV -> {
                val formatted = "${lexeme.englishWord}${exportFormat.separator}${lexeme.foreignWord}${exportFormat.separator}${lexeme.foreignContext}"

                return formatted

            }
        }
        return ""
    }

    fun exportAllLines(lexemes: List<Lexeme>, format: ExportFormat = ExportFormat.Cloze):String{
        val stringBuilder = StringBuilder()

        for (lexeme in lexemes){
            stringBuilder.append(toExportableLine(lexeme, format))
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
}