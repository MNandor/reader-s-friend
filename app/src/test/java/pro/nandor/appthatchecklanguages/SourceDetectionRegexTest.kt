package pro.nandor.appthatchecklanguages

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceDetectionRegexTest {

    @Test
    fun `Test the regex that detects Crementables`(){
        // Crement: either increment or decrement
        with(Util){
            // all of these strings contain a keyword (episode, chapter, page) or their abbreviation, followed by a number
            assertEquals(true, doesSourceContainCrementable("ep.1"))
            assertEquals(true, doesSourceContainCrementable("ep. 11"))
            assertEquals(true, doesSourceContainCrementable("episode 1"))
            assertEquals(true, doesSourceContainCrementable("Chapter 13"))
            assertEquals(true, doesSourceContainCrementable("CH.1"))
            assertEquals(true, doesSourceContainCrementable("pg. 1stuff"))
            assertEquals(true, doesSourceContainCrementable("stufyfpg. 1stuff"))
            assertEquals(true, doesSourceContainCrementable("pAgE 1.2"))
            assertEquals(true, doesSourceContainCrementable("ch. 1ch.2"))


            // these should not match
            assertEquals(false, doesSourceContainCrementable("stufyf. 1stuff"))
            assertEquals(false, doesSourceContainCrementable("34"))
            assertEquals(false, doesSourceContainCrementable("hello 34"))
            assertEquals(false, doesSourceContainCrementable("hello. 34"))
            assertEquals(false, doesSourceContainCrementable("hello 34 chapter"))
            assertEquals(false, doesSourceContainCrementable("chapter trees 5"))

        }
    }

    @Test
    fun `Test increment and decrement`(){
        with(Util){
            assertEquals("ep.2", bumpCrementable("ep.1", false))
            assertEquals("eP.2", bumpCrementable("eP.1", false))
            assertEquals("eP.0", bumpCrementable("eP.1", true))
            assertEquals("words and eP.0 and more words", bumpCrementable("words and eP.1 and more words", true))
            assertEquals("episode 16", bumpCrementable("episode 15", false))
            assertEquals("chapter 3 page 16", bumpCrementable("chapter 3 page 15", false))
        }
    }
}