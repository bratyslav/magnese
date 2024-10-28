package com.example.magnise

import com.example.magnise.model.Currency
import com.example.magnise.util.toTimestampAsIsoOffset
import org.junit.Test

import org.junit.Assert.*

class UnitTest {

    @Test
    fun currency_getInstance_theSameIsEqual() {
        val c1 = Currency.getInstance("A")
        val c2 = Currency.getInstance("A")
        assertEquals(c1, c2)
    }

    @Test
    fun currency_getInstance_otherIsNotEqual() {
        val c1 = Currency.getInstance("A")
        val c2 = Currency.getInstance("B")
        assertNotEquals(c1, c2)
    }

    @Test
    fun string_toTimestampAsIsoOffset_isCorrect() {
        val timestamp = "2024-01-01T01:00:00+00:00".toTimestampAsIsoOffset()
        assertEquals(1704070800000L, timestamp)
    }

}