package com.example.chatsunset.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatsunset.R
import com.example.chatsunset.adapters.ChatRecyclerAdapter
import com.example.chatsunset.models.Friend
import com.example.chatsunset.models.Message
import com.example.chatsunset.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    lateinit var btnPrendrePhoto :FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    lateinit var fabSendMessage :FloatingActionButton
    lateinit var fabTakePhoto :FloatingActionButton
    lateinit var editMessage :EditText
    lateinit var rvChatList : RecyclerView
    lateinit var chatRecyclerAdapter :ChatRecyclerAdapter
    val REQUEST_IMAGE_CAPTURE = 1 //Pour les permissions
    val CAMERA_PERMISSION_REQUEST_CODE = 0 // Pour les permissions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        //suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Definition des variables (authentification + elements du layout)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser

        fabTakePhoto = findViewById((R.id.fabTakePhoto))
        fabSendMessage = findViewById((R.id.fabSendMessage))
        editMessage = findViewById((R.id.editMessage))
        rvChatList = findViewById((R.id.rvChatList))

        // recuperation de l'uuid de l'ami correspondant
        val userUuid = intent.getStringExtra("friend")!!

        // recuperation de l'ami
        db.collection("users")
            .document(userUuid)
            .get()
            .addOnSuccessListener {result ->
                if(result != null){
                    var user = result.toObject(User::class.java)
                    user?.let{
                        user.uuid = userUuid
                        setUserData(user)
                    }
                }
            }.addOnFailureListener {
                Log.e("ChatActivity","erreur dans la récupération de l'utilisateur",it)
            }

    }
    //suivi de l'orientation de l'écran
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    private fun setUserData(user: User) {

        // ajout du nom de l'amis dans l'action bar
        supportActionBar?.title = user.pseudo
        chatRecyclerAdapter = ChatRecyclerAdapter()
        val messages = mutableListOf<Message>()
        rvChatList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatRecyclerAdapter
        }
        //Envoie du message
        fabSendMessage.setOnClickListener {
            val message = editMessage.text.toString()
            if(message.isNotEmpty()){
                val message = Message(
                    sender = currentUser!!.uid,
                    receiver = user.uuid,
                    text = message,
                    timestamp = System.currentTimeMillis(),
                    isReceived = false
                )
                //reset du champ texte
                editMessage.setText("")
                // cacher le clavier
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(editMessage.windowToken,0)
                //ajout du message dans la collection messages
                db.collection("messages").add(message)
                    .addOnSuccessListener {
                        rvChatList.scrollToPosition(messages.size - 1)
                    }.addOnFailureListener {
                        Log.e("ChatActivity","Erreur lors de l'envoie du message",it)
                    }
                // ajout du message dans la collection amis du current user pour avoir le message dans l'activity home
                val friend = Friend("",user.pseudo,message.text,timestamp = System.currentTimeMillis(), image = user.image ?: "")
                db.collection("users")
                    .document(currentUser!!.uid)
                    .collection("friends")
                    .document(user.uuid)
                    .set(friend)
                    .addOnSuccessListener {
                        Log.d("ChatActivity","Ami ajouté")
                    }.addOnFailureListener {
                        Log.e("ChatActivity","Erreur lors de l'enajout de l'ami",it)
                    }

                // ajout du message dans la collection amis de l'autre user pour avoir le message dans l'activity home
                db.collection("users")
                    .document(currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {result ->
                        if(result != null){
                            var user2 = result.toObject(User::class.java)
                            user2?.let{
                                user2.uuid = currentUser!!.uid
                                val friend2 = Friend("",user2.pseudo,message.text,timestamp = System.currentTimeMillis(), image = user2.image ?: "")
                                db.collection("users")
                                    .document(user.uuid)
                                    .collection("friends")
                                    .document(currentUser!!.uid)
                                    .set(friend2)
                                    .addOnSuccessListener {
                                        Log.d("ChatActivity","Ami ajouté")
                                    }.addOnFailureListener {
                                        Log.e("ChatActivity","Erreur lors de l'enajout de l'ami",it)
                                    } // A CHANGER
                            }
                        }
                    }

            }
        }

        fun dispatchTakePictureIntent() {
            val cameraPermission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                } else {
                    Toast.makeText(this, "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission de la caméra a été accordée, vous pouvez maintenant utiliser la caméra
                    dispatchTakePictureIntent() // Redéclencher l'intent après l'obtention de la permission
                } else {
                    // La permission de la caméra a été refusée, vous pouvez informer l'utilisateur ici
                    Toast.makeText(this, "Permission de la caméra refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
            }
        }

        fabTakePhoto.setOnClickListener {
            dispatchTakePictureIntent() // Appeler la fonction dispatchTakePictureIntent lorsque le bouton est cliqué
        }


        // recupertation des messages envoyés
        val sentQuery= db.collection("messages")
            .whereEqualTo("sender",currentUser!!.uid)
            .whereEqualTo("receiver",user.uuid)
            .orderBy("timestamp",Query.Direction.ASCENDING)

        //récupération des messages reçus
        val receivedQuery= db.collection("messages")
            .whereEqualTo("sender",user.uuid)
            .whereEqualTo("receiver",currentUser!!.uid)
            .orderBy("timestamp",Query.Direction.ASCENDING)

        //affichage en temps réel des messages envoyés
        sentQuery.addSnapshotListener{snapshot, exception ->
            if(exception != null){
                Log.e("ChatActivity","erreur lors de la reception des messages")
                return@addSnapshotListener
            }
            for(document in snapshot!!.documents){
                var message = document.toObject(Message::class.java)
                message?.let{
                    // pour mettre les messages à droite
                    message.isReceived = false
                    if(!messages.contains(message)){
                        messages.add(message)
                    }
                }
            }
            // affichage par ordre chronologique
            if(messages.isNotEmpty()){
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp } as MutableList<Message>
                rvChatList.scrollToPosition(messages.size - 1)
            }
        }
        //affichage en temps réel des messages envoyés
        receivedQuery.addSnapshotListener{snapShot, exception ->
            if(exception != null){
                Log.e("ChatActivity","erreur lors de la reception des messages")
                return@addSnapshotListener
            }
            for(document in snapShot!!.documents){
                var message = document.toObject(Message::class.java)
                message?.let{
                    // pour mettre les messages à gauche
                    message.isReceived = true
                    if(!messages.contains(message)){
                        messages.add(message)
                    }
                }
            }
            // affichage par ordre chronologique
            if(messages.isNotEmpty()){
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp } as MutableList<Message>
                rvChatList.scrollToPosition(messages.size - 1)
            }
        }
    }
}