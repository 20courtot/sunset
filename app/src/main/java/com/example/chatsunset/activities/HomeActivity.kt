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

        //suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Definition des variables (authentification + elements du layout)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser!!

        rvFriends = findViewById(R.id.rvFriends)
        fabChat = findViewById(R.id.fabChat)
        // click sur le bouton en bas à droite redirige vers l'activity usersSearch
        fabChat.setOnClickListener{
            Intent(this, UsersSearchActivity::class.java).also {
                startActivity(it)
            }
        }
        fabMap = findViewById(R.id.fabMap)
        //click sur le bouton en bas à gauche redirige vers l'activity map
        fabMap.setOnClickListener{
            Intent(this, MapActivity::class.java).also {
                startActivity(it)
            }
        }

    }

    //suivi de l'orientation de l'écran
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

        //recuperation de la liste des amis avec les derniers messages
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
    //ajout des boutons dans le menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home,menu)
        return super.onCreateOptionsMenu(menu)
    }

    // click sur les boutons du menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //click sur le bouton paramètre redirige vers l'activity settings
        if(item.itemId == R.id.itemSettings){
            Intent(this,SettingsActivity::class.java).also{
                startActivity(it)
            }
        }
        //click sur le bouton deconnexion appel la fonction signout de firebase et redirige vers l'activity authentification
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