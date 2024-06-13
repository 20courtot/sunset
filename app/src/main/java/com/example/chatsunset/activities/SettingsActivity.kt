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

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    lateinit var ivUser: ShapeableImageView
    lateinit var layoutTextInputEmail: TextInputLayout
    lateinit var layoutTextInputPseudo: TextInputLayout
    lateinit var btnSave: MaterialButton
    var isImageChanged = false
    var areInterestsChanged = false

    lateinit var interestCheckboxes: LinearLayout
    private val interests = listOf("Sport", "Music", "Movies", "Travel", "Games", "Reading")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Définition des variables (authentification + éléments du layout)
        auth = Firebase.auth
        db = Firebase.firestore
        currentUser = auth.currentUser

        ivUser = findViewById(R.id.ivUser)
        layoutTextInputEmail = findViewById(R.id.layoutTextInputEmail)
        layoutTextInputPseudo = findViewById(R.id.layoutTextInputPseudo)
        btnSave = findViewById(R.id.btnSave)
        interestCheckboxes = findViewById(R.id.interestCheckboxes)

        // Affichage d'un avatar par défaut
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                Glide.with(this).load(it).placeholder(R.drawable.avatar).into(ivUser)
                isImageChanged = true
            }
        }

        // Click sur l'image ouverture de la sélection de photo du téléphone
        ivUser.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Ajouter des cases à cocher pour les centres d'intérêt
        for (interest in interests) {
            val checkBox = CheckBox(this).apply {
                text = interest
                tag = interest // Ajouter un tag pour identifier la case
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            interestCheckboxes.addView(checkBox)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser!!.uid).get().addOnSuccessListener { result ->
                if (result != null) {
                    var user = result.toObject(User::class.java)
                    user?.let {
                        user.uuid = currentUser!!.uid
                        setUserData(user)
                    }
                }
            }
        } else {
            Log.d("SettingsActivity", "Pas d'utilisateur")
        }
    }

    // Suivi de l'orientation de l'écran
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    fun setUserData(user: User) {
        // Insertion de l'email et pseudo dans les inputs
        layoutTextInputEmail.editText?.setText(user.email)
        layoutTextInputPseudo.editText?.setText(user.pseudo)

        // Affichage de l'image s'il y en a une
        user.image?.let {
            Glide.with(this).load(it).placeholder(R.drawable.avatar).into(ivUser)
        }

        // Sélectionner les centres d'intérêt de l'utilisateur
        user.interests?.forEach { interest ->
            val checkBox = interestCheckboxes.findViewWithTag<CheckBox>(interest)
            checkBox?.isChecked = true
        }

        // Click sur le bouton sauvegarder
        btnSave.setOnClickListener {
            layoutTextInputPseudo.isErrorEnabled = false

            // Récupérer les centres d'intérêt sélectionnés
            val selectedInterests = mutableListOf<String>()
            for (i in 0 until interestCheckboxes.childCount) {
                val checkBox = interestCheckboxes.getChildAt(i) as CheckBox
                if (checkBox.isChecked) {
                    selectedInterests.add(checkBox.text.toString())
                }
            }

            // Vérifier si les centres d'intérêt ont changé
            areInterestsChanged = user.interests != selectedInterests
            user.interests = selectedInterests

            // Si l'image a été modifiée ou si seulement les infos ou rien n'a été modifié
            if (isImageChanged) {
                uploadImageToStorage(user)
            } else if (layoutTextInputPseudo.editText?.text.toString() != user.pseudo || areInterestsChanged) {
                updatuserData(user)
            } else {
                Toast.makeText(this, "Tout est à jour!", Toast.LENGTH_LONG).show()
                layoutTextInputPseudo.clearFocus()
            }
        }
    }

    // Ajout de l'image dans le storage
    fun uploadImageToStorage(user: User) {
        // On référence l'image avec uid de l'utilisateur pour la retrouver
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("images/${user.uuid}")

        // Récupération en bits
        val bitmap = (ivUser.drawable as BitmapDrawable).bitmap

        // Compression de l'image
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        // Upload au storage
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                user.image = uri.toString()
                // Modification aussi du pseudo
                updatuserData(user)
            }
        }
    }

    fun updatuserData(user: User) {
        // Récupération et affichage des variables modifiées
        val updatedUser = hashMapOf<String, Any>(
            "pseudo" to layoutTextInputPseudo.editText?.text.toString(),
            "image" to (user.image ?: ""),
            "interests" to user.interests!!
        )
        // Insertion dans Firebase
        db.collection("users").document(user.uuid).update(updatedUser).addOnSuccessListener {
            Toast.makeText(this, "Informations modifiées !", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            layoutTextInputPseudo.error = "Une erreur est survenue."
            layoutTextInputPseudo.isErrorEnabled = true
        }
    }
}
