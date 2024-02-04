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
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    lateinit var  rvUsers : RecyclerView
    lateinit var  editSearch : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_search)

        //suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Definition des variables (authentification + elements du layout)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser

        rvUsers = findViewById(R.id.rvUsers)
        editSearch = findViewById(R.id.editSearch)

        val usersRecyclerAdapter = UsersRecyclerAdapter()
        rvUsers.apply{
            layoutManager = LinearLayoutManager(this@UsersSearchActivity)
            adapter = usersRecyclerAdapter
        }

        val users = mutableListOf<User>()
        // recuperation de la liste des utilisateurs sans l'utilisateur connécté
        db.collection("users")
            .whereNotEqualTo("email", currentUser?.email)
            .get()
            .addOnSuccessListener {result ->
            for(document in result){
                val uuid = document.id
                val email = document.getString("email")
                val pseudo = document.getString("pseudo")
                users.add(User(uuid, email ?: "", pseudo ?: "", null))
            }
            usersRecyclerAdapter.items = users
        }.addOnFailureListener {exception ->
            Log.e("UsersSearchActivity","erreur recupération des utilisateurs",exception)
        }




        editSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            // filtrage de la liste au changement dans l'input de recherche
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                usersRecyclerAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    //suivi de l'orientation de l'écran
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}