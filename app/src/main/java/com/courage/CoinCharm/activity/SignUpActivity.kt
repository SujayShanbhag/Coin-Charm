package com.courage.CoinCharm.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.courage.notifications.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    lateinit var etConfirmPassword : EditText
    lateinit var btnSignUp : Button
    lateinit var toLogin : TextView
    lateinit var firebaseAuth : FirebaseAuth
    lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etEmail=findViewById(R.id.etEmail)
        etPassword=findViewById(R.id.etPassword)
        etConfirmPassword=findViewById(R.id.etConfirmPassword)
        toLogin=findViewById(R.id.toLogin)

        firebaseAuth=FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        btnSignUp=findViewById<Button>(R.id.btnSignUp)
        toLogin.setOnClickListener{
            intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnSignUp.setOnClickListener {
            val email=etEmail.text.toString()
            val pass = etPassword.text.toString()
            val confirmPass=etConfirmPassword.text.toString()
            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if(pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            createCollection(email)
                            intent= Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Toast.makeText(this,"Your passwords do not match",Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this,"Please enter all fields",Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun createCollection(email : String){
        val map=HashMap<String,String>()
        map.put("budget","0.00")
        map.put("balance","0.00")
        map.put("expense","0.00")
        db.collection(email).document("BBE").set(map)
    }
}