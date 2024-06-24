package com.example.chatsunset

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.chatsunset.activities.SettingsActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsActivityExpressoTest {

    @Test
    fun verifySaveButtonIsDisplayed() {
        // Lancer l'activité SettingsActivity
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)

        // Vérifier que le bouton "Enregistrer" est affiché
        onView(withId(R.id.btnSave))
            .check(matches(isDisplayed()))
    }

    @Test
    fun verifyEmailFieldIsDisplayed() {
        // Lancer l'activité SettingsActivity
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)

        // Vérifier que le champ email est affiché
        onView(withId(R.id.layoutTextInputEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun verifyInterestCheckboxesContainerIsDisplayed() {
        // Lancer l'activité SettingsActivity
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)

        // Vérifier que le conteneur de cases à cocher des intérêts est affiché
        onView(withId(R.id.interestCheckboxes))
            .check(matches(isDisplayed()))
    }
}
