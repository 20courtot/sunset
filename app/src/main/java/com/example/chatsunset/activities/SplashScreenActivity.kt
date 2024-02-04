package com.example.chatsunset.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.chatsunset.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // affichage d'une roue pendant 3s
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = Firebase.auth
            val currentUser = auth.currentUser
            //redirection vers l'activity home si un utilisateur est connécté sinon redirection vers l'activity Authentification
            if(currentUser != null){
                Intent(this, HomeActivity::class.java).also{
                    startActivity(it)
                }
            }else{
                Intent(this, AuthentificationActivity::class.java).also{
                    startActivity(it)
                }
            }

            finish()
        }, 3000)

    }
}