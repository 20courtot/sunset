package com.example.chatsunset.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.chatsunset.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthentificationActivity : AppCompatActivity() {

    lateinit var  tvRegister:TextView
    lateinit var  textInputLayoutEmail:TextInputLayout
    lateinit var  textInputLayoutPassword:TextInputLayout
    lateinit var  btnConnect:MaterialButton
    private lateinit var auth: FirebaseAuth;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentification)
        //suivi de l'orientation de l'écran
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Definition des variables (authentification + elements du layout)
        auth = Firebase.auth
        tvRegister = findViewById(R.id.tvRegister)
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail)
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword)
        btnConnect = findViewById(R.id.btnConnect)
    }
    // suivi de l'orientation de l'écran
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    override fun onStart() {
        super.onStart()
        // redirection vers l'activity register au click sur le lien
        tvRegister.setOnClickListener{
            Intent(this,RegisterActivity::class.java).also{
                startActivity(it)
            }
        }

        // click sur se connecter
        btnConnect.setOnClickListener {
            // reset des erreurs a false
            textInputLayoutPassword.isErrorEnabled = false
            textInputLayoutEmail.isErrorEnabled = false

            // recupération des inputs
            val email = textInputLayoutEmail.editText?.text.toString()
            val password = textInputLayoutPassword.editText?.text.toString()

            // verification de remplissage des champs et envoie d'erreur si vide
            if(email.isEmpty() || password.isEmpty()){
                if(password.isEmpty()){
                    textInputLayoutPassword.error = "Mot de passe requis!"
                    textInputLayoutPassword.isErrorEnabled = true
                }
                if(email.isEmpty()){
                    textInputLayoutEmail.error = "Email requis!"
                    textInputLayoutEmail.isErrorEnabled = true
                }
            }else{
                // si les champs sont remplis on redirige vers la verif des info
                signIn(email,password)
            }


        }
    }

    fun signIn(email: String,password: String){
        //verification avec la methode de firebase pour email/password
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            // si les informations sont bonnes on redirige vers l'activity home
            if(task.isSuccessful){
                Intent(this, HomeActivity::class.java).also{
                    startActivity(it)
                }
                finish()
            }else{
                //evoie d'erreur de connexion
                textInputLayoutPassword.error = "Erreur d'authentification!"
                textInputLayoutEmail.isErrorEnabled = true
                textInputLayoutPassword.isErrorEnabled = true
            }
        }
    }
}