package com.example.chatsunset

import android.content.Intent
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import com.example.chatsunset.activities.ChatActivity
import com.example.chatsunset.models.User
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatActivityRobolectricTest {

    private lateinit var activity: ChatActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var user: User
    private lateinit var messagesCollection: CollectionReference
    private lateinit var usersCollection: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var query : Query

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.getApplication())
        auth = mock(FirebaseAuth::class.java)
        db = mock(FirebaseFirestore::class.java)
        currentUser = mock(FirebaseUser::class.java)
        messagesCollection = mock(CollectionReference::class.java)
        usersCollection = mock(CollectionReference::class.java)
        documentReference = mock(DocumentReference::class.java)
        query = mock(Query::class.java)

        `when`(auth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn("dgNgHdgDojXkF7HE9XMrsSgEr6g2")
        `when`(currentUser.email).thenReturn("marwane@test.com")
        `when`(db.collection("messages")).thenReturn(messagesCollection)
        `when`(db.collection("users")).thenReturn(usersCollection)
        `when`(usersCollection.document(anyString())).thenReturn(documentReference)
        `when`(messagesCollection.add(any())).thenReturn(Tasks.forResult(mock(DocumentReference::class.java)))
        `when`(documentReference.set(any())).thenReturn(Tasks.forResult(null))

        // Mock chained methods on messagesCollection to return the query mock
        `when`(messagesCollection.whereEqualTo(anyString(), anyString())).thenReturn(query)
        `when`(query.whereEqualTo(anyString(), anyString())).thenReturn(query)
        `when`(query.orderBy(anyString(), any(Query.Direction::class.java))).thenReturn(query)
        `when`(query.get()).thenReturn(Tasks.forResult(mock(QuerySnapshot::class.java)))

        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("friend", "friendUuid")
        }

        activity = Robolectric.buildActivity(ChatActivity::class.java, intent).create().get()
        activity.auth = auth
        activity.db = db
        activity.currentUser = currentUser
    }

    @Test
    fun activityShouldNotBeNull() {
        assert(activity != null)
    }

    @Test
    fun recyclerViewShouldBeInitialized() {
        assert(activity.rvChatList != null)
    }

    @Test
    fun testSendMessageButtonClick() {
        val user = User("friendUuid", "friend@example.com", "Friend", null, listOf())
        activity.findViewById<EditText>(R.id.editMessage).setText("Hello, World!")
        activity.findViewById<FloatingActionButton>(R.id.fabSendMessage).performClick()

        // Simuler l'appel des méthodes Firestore
        activity.sendMessage(user, "Hello, World!")

        verify(messagesCollection).add(any())
        verify(documentReference).set(any())
    }

    @Test
    fun testTakePhotoButtonClick() {
        activity.findViewById<FloatingActionButton>(R.id.fabTakePhoto).performClick()
        // Here we could verify that the dispatchTakePictureIntent() was called
    }

    @Test
    fun testFetchMessages() {
        val user = User("TmJsC1GnQShuTrD1oyNtQASP9853", "laurent@test.com", "Laurent2", null, listOf())

        `when`(query.get()).thenReturn(Tasks.forResult(mock(QuerySnapshot::class.java)))
        `when`(messagesCollection.whereEqualTo("sender", currentUser.uid).whereEqualTo("receiver", user.uuid).orderBy("timestamp", Query.Direction.ASCENDING)).thenReturn(query)
        `when`(messagesCollection.whereEqualTo("sender", user.uuid).whereEqualTo("receiver", currentUser.uid).orderBy("timestamp", Query.Direction.ASCENDING)).thenReturn(query)

        activity.fetchMessages(user)

        verify(query).addSnapshotListener(any())
    }
}