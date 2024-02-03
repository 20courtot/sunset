package com.example.chatsunset.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatsunset.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;

    lateinit var btnRegister :MaterialButton
    lateinit var layoutTextInputPseudo : TextInputLayout
    lateinit var layoutTextInputEmail :TextInputLayout
    lateinit var layoutTextInputPassword :TextInputLayout
    lateinit var layoutTextInputConfirmPassword :TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        auth = Firebase.auth
        btnRegister = findViewById(R.id.btnRegister)
        layoutTextInputPseudo = findViewById(R.id.layoutTextInputPseudo)
        layoutTextInputEmail = findViewById(R.id.layoutTextInputEmail)
        layoutTextInputPassword = findViewById(R.id.layoutTextInputPassword)
        layoutTextInputConfirmPassword = findViewById(R.id.layoutTextInputConfirmPassword)

        btnRegister.setOnClickListener{
            initErrors()
            val pseudo = layoutTextInputPseudo.editText?.text.toString()
            val email = layoutTextInputEmail.editText?.text.toString()
            val password = layoutTextInputPassword.editText?.text.toString()
            val confirmPassword = layoutTextInputConfirmPassword.editText?.text.toString()

            if(pseudo.isEmpty() ||email.isEmpty() ||password.isEmpty() || confirmPassword.isEmpty()){
                if(pseudo.isEmpty()){
                    layoutTextInputPseudo.error = "Champ requis!"
                    layoutTextInputPseudo.isErrorEnabled = true
                }
                if(email.isEmpty()){
                    layoutTextInputEmail.error = "Champ requis!"
                    layoutTextInputEmail.isErrorEnabled = true
                }
                if(password.isEmpty()){
                    layoutTextInputPassword.error = "Champ requis!"
                    layoutTextInputPassword.isErrorEnabled = true
                }
                if(confirmPassword.isEmpty()){
                    layoutTextInputConfirmPassword.error = "Champ requis!"
                    layoutTextInputConfirmPassword.isErrorEnabled = true
                }
            }else{
                if(password != confirmPassword){
                    layoutTextInputConfirmPassword.error = "Le mot de passe ne correspond pas!"
                    layoutTextInputConfirmPassword.isErrorEnabled = true
                }else{
                    //creation pour modul authentification
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            // creation firestore
                            val user = hashMapOf(
                                "pseudo" to pseudo,
                                "email" to email,
                            )
                            val currentUser = auth.currentUser
                            val db = Firebase.firestore
                            db.collection("users").document(currentUser!!.uid).set(user).addOnSuccessListener {
                                Intent(this,HomeActivity::class.java).also{
                                    startActivity(it)
                                }
                            }.addOnFailureListener {
                                layoutTextInputConfirmPassword.error = "Une erreur est survenue assayer à nouveau."
                                layoutTextInputConfirmPassword.isErrorEnabled = true
                            }
                        }else{
                            layoutTextInputConfirmPassword.error = "Une erreur est survenue assayer à nouveau."
                            layoutTextInputConfirmPassword.isErrorEnabled = true
                        }
                    }
                }
            }
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    private fun initErrors(){
        layoutTextInputPseudo.isErrorEnabled = false
        layoutTextInputEmail.isErrorEnabled = false
        layoutTextInputPassword.isErrorEnabled = false
        layoutTextInputConfirmPassword.isErrorEnabled = false

    }
}