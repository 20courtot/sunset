package com.example.chatsunset

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.chatsunset.activities.ChatActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ChatActivityEspressoTest {

    @Test
    fun verifyRecyclerViewIsDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("friend", "friendUuid")
        }
        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        onView(withId(R.id.rvChatList)).check(matches(isDisplayed()))
    }

    @Test
    fun verifySendButtonIsDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("friend", "friendUuid")
        }
        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        onView(withId(R.id.fabSendMessage)).check(matches(isDisplayed()))
    }

    @Test
    fun verifyMessageInputIsDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("friend", "friendUuid")
        }
        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        onView(withId(R.id.editMessage)).check(matches(isDisplayed()))
    }

    @Test
    fun verifyTakePhotoButtonIsDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("friend", "friendUuid")
        }
        val scenario = ActivityScenario.launch<ChatActivity>(intent)
        onView(withId(R.id.fabTakePhoto)).check(matches(isDisplayed()))
    }
}
