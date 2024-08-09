package pro.nandor.appthatchecklanguages

import org.junit.Test

import org.junit.Assert.*

class ExportLexemeTest {
    private val tag = object {
        val left: String = "<b>"
        val right: String = "</b>"
    }

    val lexeme:Lexeme = Lexeme().apply {
        language = "German"
        foreignWord = "der Hund, die Hunde"
        englishWord = "dog"
        foreignContext = "Der ${tag.left}Hund${tag.right} springt."
        source = "A nice book"
    }

    @Test
    fun `Works with cloze format`(){
        var res = Util.toExportableLine(lexeme, Util.ExportFormat.Cloze)
        assertEquals("Der {{c1::Hund}} springt.", res)

    }

    @Test
    fun `Works with csv format`(){
        val res = Util.toExportableLine(lexeme, Util.ExportFormat.CSV(","))
        assertEquals("${lexeme.englishWord},${lexeme.foreignWord},${lexeme.foreignContext}", res)

        val res2 = Util.toExportableLine(lexeme, Util.ExportFormat.CSV("\t"))
        assertEquals("${lexeme.englishWord}\t${lexeme.foreignWord}\t${lexeme.foreignContext}", res2)
    }

    @Test
    fun `Works with multiple lines`(){
        val cloneLexeme = Lexeme().apply {
            englishWord = "cat"
            foreignWord = "die Katze, die Katzen"
            foreignContext = "Die ${tag.left}Katze${tag.right} springt."
        }

        val list = listOf(lexeme, cloneLexeme)

        val res = Util.exportAllLines(list, Util.ExportFormat.Cloze)
        assertEquals("Der {{c1::Hund}} springt.\nDie {{c1::Katze}} springt.\n", res)

    }

    @Test
    fun `Exports to cloze with English and Source`(){
        val res = Util.toExportableLine(lexeme, Util.ExportFormat.ClozeTranslationSource())
        assertEquals("Der {{c1::Hund}} springt.\tdog\tA nice book", res)
    }
}