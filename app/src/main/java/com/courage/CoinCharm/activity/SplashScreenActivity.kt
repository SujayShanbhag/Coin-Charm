package com.courage.CoinCharm.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.courage.notifications.R
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val icon=findViewById<ImageView>(R.id.imgSplash)

        firebaseAuth=FirebaseAuth.getInstance()
        icon.alpha=0f
        icon.animate().setDuration(1500).alpha(1f).withEndAction{
                val intent= if(firebaseAuth.currentUser!=null) Intent(this, MainActivity::class.java)
                else Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                finish()
       }
    }
}