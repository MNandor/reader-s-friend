package pro.nandor.appthatchecklanguages

import org.junit.Test

import org.junit.Assert.*

class UtilTest {
    private val tag = object {
        val left: String = "<b>"
        val right: String = "</b>"
    }

    @Test
    fun `Null on empty word`() {
        val sentence = "The quick brown fox"
        val word = ""
        val res = Util.suggestHighlightedWord(sentence, word)

        assertEquals(null, res)
    }

    @Test
    fun `Null on empty sentence`() {
        val sentence = ""
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)

        assertEquals(null, res)
    }

    @Test
    fun `Finds simple word`() {
        val sentence = "The fox jumps!"
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = "The ${tag.left}fox${tag.right} jumps!"

        assertEquals(expected, res)
    }

    @Test
    fun `Finds simple word despite different case`() {
        val sentence = "The FOX jumps!"
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = "The ${tag.left}FOX${tag.right} jumps!"

        assertEquals(expected, res)
    }

    @Test
    fun `Prefers exact match`() {
        val sentence = "The foxie, the fox and the foxous jumps!"
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = "The foxie, the ${tag.left}fox${tag.right} and the foxous jumps!"

        assertEquals(expected, res)
    }

    @Test
    fun `Prefers exact match even with punctuation`() {
        val sentence = "The foxy, the fox, and the foxous jumps!"
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = "The foxy, the ${tag.left}fox${tag.right}, and the foxous jumps!"

        assertEquals(expected, res)
    }

    @Test
    fun `Detects partial matches if no exact match is found`() {
        val sentence = "The bigfoxy, jumps!"
        val word = "fox"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = "The ${tag.left}bigfoxy${tag.right}, jumps!"

        assertEquals(expected, res)
    }

    @Test
    fun `Doesn't search inexact matches of short words`() {
        val sentence = "The bigfoxy, jumps!"
        val word = "gf"
        val res = Util.suggestHighlightedWord(sentence, word)
        val expected = null

        assertEquals(expected, res)
    }
}