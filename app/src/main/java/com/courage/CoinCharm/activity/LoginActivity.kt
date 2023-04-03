package com.courage.CoinCharm.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.courage.notifications.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    lateinit var btnLogIn : Button
    lateinit var txtSignUp : TextView
    lateinit var firebaseAuth : FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail=findViewById(R.id.etEmail)
        etPassword=findViewById(R.id.etPassword)
        txtSignUp=findViewById(R.id.txtSignUp)


        firebaseAuth=FirebaseAuth.getInstance()
        btnLogIn=findViewById<Button>(R.id.btnLogin)

        txtSignUp.setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        btnLogIn.setOnClickListener {
            val email=etEmail.text.toString()
            val pass = etPassword.text.toString()
            if(email.isNotEmpty() && pass.isNotEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else {
                Toast.makeText(this,"Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}