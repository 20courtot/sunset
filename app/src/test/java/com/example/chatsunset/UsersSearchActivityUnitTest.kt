package com.example.chatsunset

import com.example.chatsunset.util.Utils
import org.junit.Assert.assertEquals
import org.junit.Test

class UsersSearchActivityUnitTest {

    @Test
    fun calculateCommonInterests_shouldReturnCorrectCount() {
        val currentUserInterests = listOf("Sport", "Music", "Movies")
        val userInterests = listOf("Music", "Movies", "Travel")

        val commonInterests = Utils.calculateCommonInterests(currentUserInterests, userInterests)

        assertEquals(2, commonInterests)
    }
}