package com.example.chatsunset.activities

import android.content.Intent
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

        auth = Firebase.auth
        tvRegister = findViewById(R.id.tvRegister)
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail)
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword)
        btnConnect = findViewById(R.id.btnConnect)



    }

    override fun onStart() {
        super.onStart()
        tvRegister.setOnClickListener{
            Intent(this,RegisterActivity::class.java).also{
                startActivity(it)
            }
        }

        btnConnect.setOnClickListener {
            textInputLayoutPassword.isErrorEnabled = false
            textInputLayoutEmail.isErrorEnabled = false

            val email = textInputLayoutEmail.editText?.text.toString()
            val password = textInputLayoutPassword.editText?.text.toString()

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
                signIn(email,password)
            }


        }
    }

    fun signIn(email: String,password: String){

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Intent(this, HomeActivity::class.java).also{
                    startActivity(it)
                }
                finish()
            }else{
                textInputLayoutPassword.error = "Erreur d'authentification!"
                textInputLayoutEmail.isErrorEnabled = true
                textInputLayoutPassword.isErrorEnabled = true
            }
        }
    }
}