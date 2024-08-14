package pro.nandor.appthatchecklanguages

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceDetectionRegexTest {

    @Test
    fun `Test the regex that detects Crementables`(){
        // Crement: either increment or decrement
        with(Util::doesSourceContainCrementable){
            // all of these strings contain a keyword (episode, chapter, page) or their abbreviation, followed by a number
            assertEquals(true, this("ep.1"))
            assertEquals(true, this("ep. 11"))
            assertEquals(true, this("episode 1"))
            assertEquals(true, this("Chapter 13"))
            assertEquals(true, this("CH.1"))
            assertEquals(true, this("pg. 1stuff"))
            assertEquals(true, this("stufyfpg. 1stuff"))
            assertEquals(true, this("pAgE 1.2"))
            assertEquals(true, this("ch. 1ch.2"))


            // these should not match
            assertEquals(false, this("stufyf. 1stuff"))
            assertEquals(false, this("34"))
            assertEquals(false, this("hello 34"))
            assertEquals(false, this("hello. 34"))
            assertEquals(false, this("hello 34 chapter"))
            assertEquals(false, this("chapter trees 5"))

        }
    }

    @Test
    fun `Test increment and decrement`(){
        with(Util::bumpCrementable){
            assertEquals("ep.2", this("ep.1", false))
            assertEquals("eP.2", this("eP.1", false))
            assertEquals("eP.0", this("eP.1", true))
            assertEquals("words and eP.0 and more words", this("words and eP.1 and more words", true))
            assertEquals("episode 16", this("episode 15", false))
            assertEquals("chapter 3 page 16", this("chapter 3 page 15", false))
        }
    }
}