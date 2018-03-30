package com.bori.hipe

import com.bori.hipe.models.Tuple
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ExampleUnitTest {

    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())

        val s = Tuple(listOf(1, 2, 4), setOf(1, "hhh"))
        val js = Gson().toJson(s)

        val t = Tuple::class.java
        print(Gson().fromJson(js, t)._2!!::class.java.canonicalName)
    }

    companion object {

        private val TAG = "ExampleUnitTest"
    }
}