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

        // as a last resort, we use a LCD algorithm
        val anyWordRegex = "\\b\\p{L}+\\b".toRegex()

        // note: we use \\p{L}
        // instead of \\w
        // to support foreign-language characters

        val allSentenceWords = anyWordRegex.findAll(sentence)

        val lcsResults = allSentenceWords.associate { it to longestCommonSubsequence(it.value, word) }

        val (bestMatch, bestMatchStrength) = lcsResults.maxBy { it.value }

        if (bestMatchStrength > 3){
            val wordInSentence = _sentence.substring(bestMatch.range)
            val newSentence = _sentence.replaceRange(bestMatch.range, "${tag.left}$wordInSentence${tag.right}")

            return newSentence
        }

        return  null
    }

    fun longestCommonSubsequence(text1: String, text2: String): Int {
        val m = text1.length
        val n = text2.length

        // safeguard for computations taking too long
        if (n*m>400) return 0

        // Create a 2D array to store lengths of longest common subsequence.
        val dp = Array(m + 1) { IntArray(n + 1) }

        // Build the dp array from bottom up.
        for (i in 1..m) {
            for (j in 1..n) {
                if (text1[i - 1] == text2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        // The length of the longest common subsequence will be in dp[m][n].
        return dp[m][n]
    }

    sealed interface ExportFormat{

        object Cloze: ExportFormat

        data class ClozeTranslationSource(val separator: String = "\t"): ExportFormat
        data class CSV(val separator: String = "\t"): ExportFormat
    }
    fun toExportableLine(lexeme: Lexeme, exportFormat:ExportFormat = ExportFormat.ClozeTranslationSource()):String{

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
            is ExportFormat.ClozeTranslationSource -> {
                val baseSentence = lexeme.foreignContext
                val exportSentence = baseSentence.replace(tag.left, "{{c1::").replace(tag.right, "}}")

                val exportable = "$exportSentence${exportFormat.separator}${lexeme.englishWord}${exportFormat.separator}${lexeme.source}"

                return exportable
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

    fun doesSourceContainCrementable(testableString: String): Boolean {
        val regex = Regex(pattern = "(chapter|ch|episode|e|ep|page|p|pg)\\.? ?(\\d+)", RegexOption.IGNORE_CASE)

        return regex.find(testableString) != null
    }
}