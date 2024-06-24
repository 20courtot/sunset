package com.example.chatsunset

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chatsunset.activities.AuthentificationActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.not
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthentificationActivityEspressoTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<AuthentificationActivity> = ActivityScenarioRule(AuthentificationActivity::class.java)

    companion object {
        @BeforeClass
        @JvmStatic
        fun disableAnimations() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand("settings put global window_animation_scale 0")
            instrumentation.uiAutomation.executeShellCommand("settings put global transition_animation_scale 0")
            instrumentation.uiAutomation.executeShellCommand("settings put global animator_duration_scale 0")
        }
    }

    @Test
    fun verifyUIElementsAreDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthentificationActivity::class.java)
        val scenario = ActivityScenario.launch<AuthentificationActivity>(intent)
        onView(isRoot()).perform(waitFor(1000))
        onView(withId(R.id.textInputLayoutEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.textInputLayoutPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnConnect)).check(matches(isDisplayed()))
        onView(withId(R.id.tvRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun verifyEmailAndPasswordFieldsShowErrorWhenEmpty() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthentificationActivity::class.java)
        val scenario = ActivityScenario.launch<AuthentificationActivity>(intent)
        onView(isRoot()).perform(waitFor(1000))
        onView(withId(R.id.btnConnect)).perform(click())
        printErrorText(R.id.textInputLayoutEmail)
        printErrorText(R.id.textInputLayoutPassword)
        onView(withId(R.id.textInputLayoutEmail)).check(matches(hasTextInputLayoutErrorText("Email requis!")))
        onView(withId(R.id.textInputLayoutPassword)).check(matches(hasTextInputLayoutErrorText("Mot de passe requis!")))
    }

    @Test
    fun verifyRegisterButtonWorks() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthentificationActivity::class.java)
        val scenario = ActivityScenario.launch<AuthentificationActivity>(intent)
        onView(isRoot()).perform(waitFor(1000))
        onView(withId(R.id.tvRegister)).perform(click())
        // Vérifiez que l'intent pour RegisterActivity est lancé
    }

    @Test
    fun verifyValidInputsDoNotShowErrorMessages() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthentificationActivity::class.java)
        val scenario = ActivityScenario.launch<AuthentificationActivity>(intent)
        onView(isRoot()).perform(waitFor(1000))
        onView(isEditTextInLayout(R.id.textInputLayoutEmail)).perform(typeText("test@example.com"))
        onView(isEditTextInLayout(R.id.textInputLayoutPassword)).perform(typeText("password"))
        onView(withId(R.id.btnConnect)).perform(click())
        printErrorText(R.id.textInputLayoutEmail)
        printErrorText(R.id.textInputLayoutPassword)
        onView(withId(R.id.textInputLayoutEmail)).check(matches(not(hasTextInputLayoutErrorText())))
        onView(withId(R.id.textInputLayoutPassword)).check(matches(not(hasTextInputLayoutErrorText())))
//        assert(true)
    }



//    @Test
//    fun verifyInvalidEmailFormatShowsErrorMessage() {
//        val intent = Intent(ApplicationProvider.getApplicationContext(), AuthentificationActivity::class.java)
//        val scenario = ActivityScenario.launch<AuthentificationActivity>(intent)
//        onView(isRoot()).perform(waitFor(1000))
//        onView(isEditTextInLayout(R.id.textInputLayoutEmail)).perform(typeText("invalidemail"))
//        onView(isEditTextInLayout(R.id.textInputLayoutPassword)).perform(typeText("password"))
//        onView(withId(R.id.btnConnect)).perform(click())
//        printErrorText(R.id.textInputLayoutEmail)
//        printErrorText(R.id.textInputLayoutPassword)
////        onView(withId(R.id.textInputLayoutEmail)).check(matches(hasTextInputLayoutErrorText("Format d'email invalide!")))
//        assert(true)
//    }

    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "wait for $millis milliseconds"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    private fun isEditTextInLayout(parentViewId: Int): Matcher<View> {
        return allOf(
            isDescendantOfA(withId(parentViewId)),
            withClassName(endsWith("EditText"))
        )
    }

    private fun hasTextInputLayoutErrorText(expectedError: String?): Matcher<View> {
        return object : BaseMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("with error: $expectedError")
            }

            override fun matches(item: Any): Boolean {
                if (item !is TextInputLayout) return false
                val error = item.error ?: return false
                val errorText = error.toString()
                Log.d("EspressoTest", "Expected error: $expectedError, Actual error: $errorText")
                return expectedError == errorText
            }
        }
    }

    private fun hasTextInputLayoutErrorText(): Matcher<View> {
        return object : BaseMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("with no error text")
            }

            override fun matches(item: Any): Boolean {
                if (item !is TextInputLayout) return false
                Log.d("EspressoTest", "Actual error: ${item.error}")
                return item.error == null
            }
        }
    }

    private fun printErrorText(viewId: Int) {
        onView(withId(viewId)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextInputLayout::class.java)
            }

            override fun getDescription(): String {
                return "Print error text"
            }

            override fun perform(uiController: UiController, view: View) {
                val textInputLayout = view as TextInputLayout
                Log.d("EspressoTest", "Error text for view $viewId: ${textInputLayout.error}")
            }
        })
    }
}
