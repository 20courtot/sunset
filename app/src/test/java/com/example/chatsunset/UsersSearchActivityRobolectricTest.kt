package com.example.chatsunset

import com.example.chatsunset.activities.UsersSearchActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UsersSearchActivityRobolectricTest {

    private lateinit var activity: UsersSearchActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockQuery: Query
    private lateinit var mockSnapshot: QuerySnapshot
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.getApplication())

        auth = mock(FirebaseAuth::class.java)
        db = mock(FirebaseFirestore::class.java)
        currentUser = mock(FirebaseUser::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockQuery = mock(Query::class.java)
        mockSnapshot = mock(QuerySnapshot::class.java)
        mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

        `when`(auth.currentUser).thenReturn(currentUser)
        `when`(currentUser.uid).thenReturn("123")
        `when`(currentUser.email).thenReturn("test@example.com")

        `when`(db.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereNotEqualTo(anyString(), anyString())).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockSnapshot))

        val documents = listOf(mockDocumentSnapshot)
        `when`(mockSnapshot.documents).thenReturn(documents)

        `when`(mockDocumentSnapshot.id).thenReturn("123")
        `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@example.com")
        `when`(mockDocumentSnapshot.getString("pseudo")).thenReturn("TestUser")
        `when`(mockDocumentSnapshot.get("interests")).thenReturn(listOf("Sport", "Music"))

        activity = Robolectric.buildActivity(UsersSearchActivity::class.java).create().get()
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
        assert(activity.rvUsers != null)
    }

    @Test
    fun calculateCommonInterests_shouldReturnCorrectCount() {
        // Mocking data for User
        val currentUserInterests = listOf("Sport", "Music", "Movies")
        val userInterests = listOf("Sport", "Music")

        // Testing the method directly
        val commonInterests = activity.calculateCommonInterests(currentUserInterests, userInterests)
        assert(commonInterests == 2)
    }

    @Test
    fun fetchUsers_shouldReturnUsers() {
        activity.fetchUsers(listOf("Sport", "Music"))
        val itemCount = activity.usersRecyclerAdapter.itemCount
        assert(activity.usersRecyclerAdapter.itemCount == 0)
    }
}
