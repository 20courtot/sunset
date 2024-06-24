package com.example.chatsunset

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.chatsunset.activities.UsersSearchActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class UsersSearchActivityExpressoTest {

    @Test
    fun verifyRecyclerViewIsDisplayed() {
        val scenario = ActivityScenario.launch(UsersSearchActivity::class.java)
        onView(withId(R.id.rvUsers)).check(matches(isDisplayed()))
    }

    @Test
    fun verifyEditSearchIsDisplayed() {
        val scenario = ActivityScenario.launch(UsersSearchActivity::class.java)
        onView(withId(R.id.editSearch)).check(matches(isDisplayed()))
    }
}
