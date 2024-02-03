package com.example.chatsunset.activities

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    lateinit var fabSendMessage :FloatingActionButton
    lateinit var editMessage :EditText
    lateinit var rvChatList : RecyclerView
    lateinit var chatRecyclerAdapter :ChatRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        fabSendMessage = findViewById((R.id.fabSendMessage))
        editMessage = findViewById((R.id.editMessage))
        rvChatList = findViewById((R.id.rvChatList))

        val userUuid = intent.getStringExtra("friend")!!

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
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    private fun setUserData(user: User) {

        supportActionBar?.title = user.pseudo
        chatRecyclerAdapter = ChatRecyclerAdapter()
        // Message(sender = "Lucas", receiver = "Morgan",text = "Salut 1",timestamp = 123456789, isReceived = false),
        val messages = mutableListOf<Message>()
        rvChatList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatRecyclerAdapter
        }
        fabSendMessage.setOnClickListener {
            // envoie
            val message = editMessage.text.toString()
            if(message.isNotEmpty()){
                val message = Message(
                    sender = currentUser!!.uid,
                    receiver = user.uuid,
                    text = message,
                    timestamp = System.currentTimeMillis(),
                    isReceived = false
                )
                editMessage.setText("")
                // cacher le clavier
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(editMessage.windowToken,0)

                db.collection("messages").add(message)
                    .addOnSuccessListener {
                        rvChatList.scrollToPosition(messages.size - 1)
                    }.addOnFailureListener {
                        Log.e("ChatActivity","Erreur lors de l'envoie du message",it)
                    }
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

        // chatRecyclerAdapter.items = messages

        val sentQuery= db.collection("messages")
            .whereEqualTo("sender",currentUser!!.uid)
            .whereEqualTo("receiver",user.uuid)
            .orderBy("timestamp",Query.Direction.ASCENDING)

        val receivedQuery= db.collection("messages")
            .whereEqualTo("sender",user.uuid)
            .whereEqualTo("receiver",currentUser!!.uid)
            .orderBy("timestamp",Query.Direction.ASCENDING)

        sentQuery.addSnapshotListener{snapshot, exception ->
            if(exception != null){
                Log.e("ChatActivity","erreur lors de la reception des messages")
                return@addSnapshotListener
            }
            for(document in snapshot!!.documents){
                var message = document.toObject(Message::class.java)
                message?.let{
                    message.isReceived = false
                    if(!messages.contains(message)){
                        messages.add(message)
                    }
                }
            }
            if(messages.isNotEmpty()){
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp } as MutableList<Message>
                rvChatList.scrollToPosition(messages.size - 1)
            }
        }

        receivedQuery.addSnapshotListener{snapShot, exception ->
            if(exception != null){
                Log.e("ChatActivity","erreur lors de la reception des messages")
                return@addSnapshotListener
            }
            for(document in snapShot!!.documents){
                var message = document.toObject(Message::class.java)
                message?.let{
                    message.isReceived = true
                    if(!messages.contains(message)){
                        messages.add(message)
                    }
                }
            }
            if(messages.isNotEmpty()){
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp } as MutableList<Message>
                rvChatList.scrollToPosition(messages.size - 1)
            }
        }
    }
}