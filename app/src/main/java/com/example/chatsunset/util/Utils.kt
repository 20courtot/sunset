package com.example.chatsunset.util

object Utils {
    fun calculateCommonInterests(currentUserInterests: List<String>, userInterests: List<String>): Int {
        return currentUserInterests.intersect(userInterests).size
    }
}