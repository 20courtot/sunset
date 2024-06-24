package com.example.chatsunset

import android.content.Intent
import com.example.chatsunset.activities.AuthentificationActivity
import com.example.chatsunset.activities.RegisterActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthentificationActivityRobolectricTest {

    private lateinit var activity: AuthentificationActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    @Before
    fun setup() {
        FirebaseApp.initializeApp(RuntimeEnvironment.getApplication())
        auth = mock(FirebaseAuth::class.java)
        currentUser = mock(FirebaseUser::class.java)

        `when`(auth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn("123")
        `when`(currentUser.email).thenReturn("test@example.com")

        activity = Robolectric.buildActivity(AuthentificationActivity::class.java).create().get()
        activity.auth = auth
    }

    @Test
    fun activityShouldNotBeNull() {
        assert(activity != null)
    }

    @Test
    fun verifyOnStartSetsUpClickListener() {
        val intent = Intent(activity, RegisterActivity::class.java)
        activity.tvRegister.performClick()
        val shadowActivity = Shadows.shadowOf(activity)
        val nextIntent = shadowActivity.nextStartedActivity

        assert(nextIntent != null) { "Expected an intent to be started" }
        assert(nextIntent.filterEquals(intent)) { "Expected intent to be started" }
    }

    @Test
    fun verifySignInWithEmailAndPasswordIsCalled() {
        val email = "test@example.com"
        val password = "password"

        activity.signIn(email, password)

        verify(auth).signInWithEmailAndPassword(email, password)
    }
}
