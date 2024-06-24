package com.example.chatsunset.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.chatsunset.R
import com.example.chatsunset.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    var currentUser: FirebaseUser? = null

    lateinit var ivUser: ShapeableImageView
    lateinit var layoutTextInputEmail: TextInputLayout
    lateinit var layoutTextInputPseudo: TextInputLayout
    lateinit var btnSave: MaterialButton
    lateinit var interestCheckboxes: LinearLayout

    var isImageChanged = false
    var areInterestsChanged = false
    private val interests = listOf("Sport", "Music", "Movies", "Travel", "Games", "Reading")
    lateinit var pickImage: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeScreenOrientation()
        initializeFirebase()
        setupUI()
    }

    private fun initializeScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    fun initializeFirebase() {
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser
        storage = Firebase.storage
    }

    private fun setupUI() {
        ivUser = findViewById(R.id.ivUser)
        layoutTextInputEmail = findViewById(R.id.layoutTextInputEmail)
        layoutTextInputPseudo = findViewById(R.id.layoutTextInputPseudo)
        btnSave = findViewById(R.id.btnSave)
        interestCheckboxes = findViewById(R.id.interestCheckboxes)

        setupDefaultImagePicker()
        setupProfileImageClickListener()
        populateInterestsCheckboxes()

        loadUserData()
    }

    fun setupDefaultImagePicker() {
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                Glide.with(this).load(it).placeholder(R.drawable.avatar).into(ivUser)
                isImageChanged = true
            }
        }
    }

    private fun setupProfileImageClickListener() {
        ivUser.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    fun populateInterestsCheckboxes() {
        interests.forEach { interest ->
            val checkBox = CheckBox(this).apply {
                text = interest
                tag = interest
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            interestCheckboxes.addView(checkBox)
        }
    }

    fun loadUserData() {
        if (currentUser != null) {
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { result ->
                result.toObject(User::class.java)?.let { user ->
                    user.uuid = currentUser!!.uid
                    setUserData(user)
                }
            }
        } else {
            Log.d("SettingsActivity", "No user logged in")
        }
    }

    fun setUserData(user: User) {
        layoutTextInputEmail.editText?.setText(user.email)
        layoutTextInputPseudo.editText?.setText(user.pseudo)
        if (user.image != null) {
            Glide.with(this).load(user.image).placeholder(R.drawable.avatar).into(ivUser)
        } else {
            Glide.with(this).load(R.drawable.avatar).into(ivUser)
        }
        user.interests?.forEach { interest ->
            interestCheckboxes.findViewWithTag<CheckBox>(interest)?.isChecked = true
        }
        setupSaveButtonClickListener(user)
    }

    private fun setupSaveButtonClickListener(user: User) {
        btnSave.setOnClickListener {
            saveUserData(user)
        }
    }

    fun saveUserData(user: User) {
        val selectedInterests = collectSelectedInterests()
        user.interests = selectedInterests
        checkInterestChanges(user)
        handleUserDataUpdate(user)
    }

    private fun collectSelectedInterests(): MutableList<String> {
        val selectedInterests = mutableListOf<String>()
        for (i in 0 until interestCheckboxes.childCount) {
            (interestCheckboxes.getChildAt(i) as? CheckBox)?.let {
                if (it.isChecked) {
                    selectedInterests.add(it.text.toString())
                }
            }
        }
        return selectedInterests
    }

    private fun checkInterestChanges(user: User) {
        val currentInterests = collectSelectedInterests()
        areInterestsChanged = user.interests != currentInterests
    }

    private fun handleUserDataUpdate(user: User) {
        if (isImageChanged) {
            uploadImageToStorage(user)
        } else if (layoutTextInputPseudo.editText?.text.toString() != user.pseudo || areInterestsChanged) {
            updateUserData(user)
        } else {
            Toast.makeText(this, "All information is up to date!", Toast.LENGTH_LONG).show()
            layoutTextInputPseudo.clearFocus()
        }
    }

    private fun uploadImageToStorage(user: User) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("images/${user.uuid}")
        val bitmap = (ivUser.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                user.image = uri.toString()
                updateUserData(user)
            }
        }
    }

    private fun updateUserData(user: User) {
        val updatedUser = hashMapOf(
            "pseudo" to layoutTextInputPseudo.editText?.text.toString(),
            "image" to (user.image ?: ""),
            "interests" to user.interests!!
        )
        db.collection("users").document(user.uuid).update(updatedUser).addOnSuccessListener {
            Toast.makeText(this, "Informations modifiées !", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            layoutTextInputPseudo.error = "Une erreur est survenue."
            layoutTextInputPseudo.isErrorEnabled = true
        }
    }
}
