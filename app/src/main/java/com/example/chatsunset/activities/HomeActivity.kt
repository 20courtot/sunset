package com.example.chatsunset.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatsunset.R
import com.example.chatsunset.adapters.FriendsRecyclerAdaper
import com.example.chatsunset.models.Friend
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    lateinit var rvFriends: RecyclerView
    lateinit var fabChat: FloatingActionButton
    lateinit var fabMap: FloatingActionButton
    lateinit var friendsRecyclerAdaper: FriendsRecyclerAdaper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser!!

        rvFriends = findViewById(R.id.rvFriends)
        fabChat = findViewById(R.id.fabChat)
        fabChat.setOnClickListener{
            Intent(this, UsersSearchActivity::class.java).also {
                startActivity(it)
            }
        }
        fabMap = findViewById(R.id.fabMap)
        fabMap.setOnClickListener{
            Intent(this, MapActivity::class.java).also {
                startActivity(it)
            }
        }

    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    override fun onResume() {
        super.onResume()

        val friends = mutableListOf<Friend>()

        friendsRecyclerAdaper = FriendsRecyclerAdaper()
        friendsRecyclerAdaper.items = friends
        rvFriends.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = friendsRecyclerAdaper
        }

        //recuperation des derniers messages
        db.collection("users")
            .document(currentUser!!.uid)
            .collection("friends").get()
            .addOnSuccessListener {result ->
                for (document in result){
                    val friend = document.toObject(Friend::class.java)
                    friend.uuid = document.id
                    friends.add(friend)
                }
                friendsRecyclerAdaper.items = friends
            }.addOnFailureListener {
                Log.e("HomeActivity", "erreur lors de la lecture de la liste d'amis",it)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.itemSettings){
            Intent(this,SettingsActivity::class.java).also{
                startActivity(it)
            }
        }

        if(item.itemId == R.id.itemLogout){
            val auth = Firebase.auth
            auth.signOut()
            Intent(this,AuthentificationActivity::class.java).also{
                startActivity(it)
            }
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}