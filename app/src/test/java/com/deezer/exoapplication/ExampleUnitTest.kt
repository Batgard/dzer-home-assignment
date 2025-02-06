package com.deezer.exoapplication

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val list = mutableListOf(1, 2, 4)
        val list2 = list - 3
        assertEquals(list, list2)
    }
}