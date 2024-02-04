package com.example.chatsunset.activities

import android.app.Instrumentation.ActivityResult
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private var currentUser:FirebaseUser? = null


    lateinit var ivUser :ShapeableImageView
    private lateinit var layoutTextInputEmail : TextInputLayout
    private lateinit var layoutTextInputPseudo : TextInputLayout
    lateinit var btnSave : MaterialButton
    var isImageChanged = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Definition des variables (authentification + elements du layout)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser

        ivUser = findViewById(R.id.ivUser)
        layoutTextInputEmail = findViewById(R.id.layoutTextInputEmail)
        layoutTextInputPseudo = findViewById(R.id.layoutTextInputPseudo)
        btnSave = findViewById(R.id.btnSave)

        // Affichage d'un avatar par défaut
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            it?.let{
                Glide.with(this).load(it).placeholder(R.drawable.avatar).into(ivUser)
                isImageChanged = true
            }
        }

        // click sur l'image ouverture de la selection de photo du telephone
        ivUser.setOnClickListener{
            pickImage.launch("image/*")
        }

        if(currentUser != null){
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { result->
                if(result != null){
                    var user = result.toObject(User::class.java)
                    user?.let{
                        user.uuid = currentUser!!.uid
                        setUserData(user)
                    }
                }

            }
        }else{
            Log.d("SettingsActivity", "PAs d'utilisateur")
        }
    }

    //suivi de l'orientation de l'écran
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    private fun setUserData(user : User){
        // insertion de l'email et pseudo dans les inputs
        layoutTextInputEmail.editText?.setText(user.email)
        layoutTextInputPseudo.editText?.setText(user.pseudo)

        // Affichage de l'image si il y en a une
        user.image?.let{
            Glide.with(this).load(it).placeholder(R.drawable.avatar).into(ivUser)
        }

        // click sur le bouton sauvegarder
        btnSave.setOnClickListener {
            layoutTextInputPseudo.isErrorEnabled = false

            //Si l'image a été modifiée ou si seulement les infos ou rien n'a été modifié
            if(isImageChanged){
                uploadImageToStorage(user)
            }else if(layoutTextInputPseudo.editText?.text.toString() != user.pseudo){
                updatuserData(user)
            }else{
                Toast.makeText(this, "Tout est à jour!",Toast.LENGTH_LONG).show()
                layoutTextInputPseudo.clearFocus()
            }
        }
    }

    // Ajout de l'image dans le storage
    private fun uploadImageToStorage(user: User) {
        // On reference l'image aves uid de l'utilisateur pour la retrouvé
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("images/${user.uuid}")

        // récupératio en bits
        val bitmap = (ivUser.drawable as BitmapDrawable).bitmap

        // compression de l'image
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        //upload au storage
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                user.image =  uri.toString()
                // modif aussi du pseudo
                updatuserData(user)
            }
        }
    }

    private fun updatuserData(user: User) {
        //Récupération et affichage des variables modifiées
        var updatedUser = hashMapOf<String ,Any>(
            "pseudo" to layoutTextInputPseudo.editText?.text.toString(),
            "image" to (user.image ?:"")
        )
        // insertion dans firebase
        db.collection("users").document(user.uuid).update(updatedUser).addOnSuccessListener {
            Toast.makeText(this, "Informations modifiées !",Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            layoutTextInputPseudo.error = "Une erreur est survenu."
            layoutTextInputPseudo.isErrorEnabled = true
        }
    }
}