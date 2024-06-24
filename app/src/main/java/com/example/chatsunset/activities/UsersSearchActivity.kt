package com.example.chatsunset.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatsunset.R
import com.example.chatsunset.adapters.UsersRecyclerAdapter
import com.example.chatsunset.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UsersSearchActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var currentUser: FirebaseUser? = null

    lateinit var rvUsers: RecyclerView
    lateinit var editSearch: EditText
    lateinit var usersRecyclerAdapter: UsersRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_search)

        setupUI()
        setupFirebase()
        setupRecyclerView()
        setupSearchListener()

        currentUser?.let { user ->
            fetchCurrentUserInterests(user.uid) { currentUserInterests ->
                fetchUsers(currentUserInterests)
            }
        }
    }

    private fun setupUI() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        rvUsers = findViewById(R.id.rvUsers)
        editSearch = findViewById(R.id.editSearch)
    }

    private fun setupFirebase() {
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser
    }

    private fun setupRecyclerView() {
        usersRecyclerAdapter = UsersRecyclerAdapter()
        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UsersSearchActivity)
            adapter = usersRecyclerAdapter
        }
    }

    private fun setupSearchListener() {
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                usersRecyclerAdapter.filter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchCurrentUserInterests(userId: String, callback: (List<String>) -> Unit) {
        db.collection("users").document(userId).get().addOnSuccessListener { result ->
            val currentUserInterests = result.get("interests") as? List<String> ?: listOf()
            callback(currentUserInterests)
        }
    }

    fun fetchUsers(currentUserInterests: List<String>) {
        db.collection("users")
            .whereNotEqualTo("email", currentUser?.email)
            .get()
            .addOnSuccessListener { result ->
                val users = result.map { document ->
                    User(
                        uuid = document.id,
                        email = document.getString("email") ?: "",
                        pseudo = document.getString("pseudo") ?: "",
                        image = null,
                        interests = document.get("interests") as? List<String> ?: listOf()
                    )
                }
                updateRecyclerView(users, currentUserInterests)
            }.addOnFailureListener { exception ->
                Log.e("UsersSearchActivity", "Erreur récupération des utilisateurs", exception)
            }
    }

    private fun updateRecyclerView(users: List<User>, currentUserInterests: List<String>) {
        val sortedUsers = users.sortedByDescending { user ->
            calculateCommonInterests(currentUserInterests, user.interests ?: listOf())
        }.take(10)
        usersRecyclerAdapter.items = sortedUsers.toMutableList()
    }

    fun calculateCommonInterests(currentUserInterests: List<String>, userInterests: List<String>): Int {
        return currentUserInterests.intersect(userInterests).size
    }
}
