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
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    lateinit var btnPrendrePhoto: FloatingActionButton
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var currentUser: FirebaseUser? = null
    lateinit var fabSendMessage: FloatingActionButton
    lateinit var fabTakePhoto: FloatingActionButton
    lateinit var editMessage: EditText
    lateinit var rvChatList: RecyclerView
    lateinit var chatRecyclerAdapter: ChatRecyclerAdapter

    val REQUEST_IMAGE_CAPTURE = 1
    val CAMERA_PERMISSION_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initializeUI()
        initializeFirebase()
        setupRecyclerView()

        val userUuid = intent.getStringExtra("friend")!!
        fetchUser(userUuid)
    }

    fun initializeUI() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        fabTakePhoto = findViewById(R.id.fabTakePhoto)
        fabSendMessage = findViewById(R.id.fabSendMessage)
        editMessage = findViewById(R.id.editMessage)
        rvChatList = findViewById(R.id.rvChatList)
    }

    fun initializeFirebase() {
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser
    }

    fun setupRecyclerView() {
        chatRecyclerAdapter = ChatRecyclerAdapter()
        rvChatList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatRecyclerAdapter
        }
    }

    fun fetchUser(userUuid: String) {
        db.collection("users")
            .document(userUuid)
            .get()
            .addOnSuccessListener { result ->
                if (result != null) {
                    val user = result.toObject(User::class.java)
                    user?.let {
                        it.uuid = userUuid
                        setUserData(it)
                    }
                }
            }.addOnFailureListener {
                Log.e("ChatActivity", "Erreur dans la récupération de l'utilisateur", it)
            }
    }

    fun setUserData(user: User) {
        supportActionBar?.title = user.pseudo

        fabSendMessage.setOnClickListener {
            val messageText = editMessage.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(user, messageText)
            }
        }

        fabTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        fetchMessages(user)
    }

    fun sendMessage(user: User, messageText: String) {
        val message = Message(
            sender = currentUser!!.uid,
            receiver = user.uuid,
            text = messageText,
            timestamp = System.currentTimeMillis(),
            isReceived = false
        )
        editMessage.setText("")

        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editMessage.windowToken, 0)

        db.collection("messages").add(message)
            .addOnSuccessListener {
                rvChatList.scrollToPosition(chatRecyclerAdapter.itemCount - 1)
            }.addOnFailureListener {
                Log.e("ChatActivity", "Erreur lors de l'envoie du message", it)
            }
        updateFriendList(user, messageText)
    }

    fun updateFriendList(user: User, messageText: String) {
        val friend = Friend("", user.pseudo, messageText, timestamp = System.currentTimeMillis(), image = user.image ?: "")
        db.collection("users")
            .document(currentUser!!.uid)
            .collection("friends")
            .document(user.uuid)
            .set(friend)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Ami ajouté")
            }.addOnFailureListener {
                Log.e("ChatActivity", "Erreur lors de l'ajout de l'ami", it)
            }

        db.collection("users")
            .document(user.uuid)
            .collection("friends")
            .document(currentUser!!.uid)
            .set(friend)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Ami ajouté")
            }.addOnFailureListener {
                Log.e("ChatActivity", "Erreur lors de l'ajout de l'ami", it)
            }
    }

    fun fetchMessages(user: User) {
        val messages = mutableListOf<Message>()
        val sentQuery = db.collection("messages")
            .whereEqualTo("sender", currentUser!!.uid)
            .whereEqualTo("receiver", user.uuid)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val receivedQuery = db.collection("messages")
            .whereEqualTo("sender", user.uuid)
            .whereEqualTo("receiver", currentUser!!.uid)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        sentQuery.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("ChatActivity", "Erreur lors de la réception des messages")
                return@addSnapshotListener
            }
            for (document in snapshot!!.documents) {
                val message = document.toObject(Message::class.java)
                message?.let {
                    it.isReceived = false
                    if (!messages.contains(it)) {
                        messages.add(it)
                    }
                }
            }
            if (messages.isNotEmpty()) {
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp }.toMutableList()
                rvChatList.scrollToPosition(messages.size - 1)
            }
        }

        receivedQuery.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("ChatActivity", "Erreur lors de la réception des messages")
                return@addSnapshotListener
            }
            for (document in snapshot!!.documents) {
                val message = document.toObject(Message::class.java)
                message?.let {
                    it.isReceived = true
                    if (!messages.contains(it)) {
                        messages.add(it)
                    }
                }
            }
            if (messages.isNotEmpty()) {
                chatRecyclerAdapter.items = messages.sortedBy { it.timestamp }.toMutableList()
                rvChatList.scrollToPosition(messages.size - 1)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Permission de la caméra refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
        }
    }

    fun calculateCommonInterests(currentUserInterests: List<String>, userInterests: List<String>): Int {
        return currentUserInterests.intersect(userInterests).size
    }
}
