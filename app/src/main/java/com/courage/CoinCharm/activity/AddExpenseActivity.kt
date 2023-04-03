package com.courage.CoinCharm.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.courage.notifications.R
import com.courage.CoinCharm.model.Expense
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class AddExpenseActivity : AppCompatActivity() {
    lateinit var etPlace : EditText
    lateinit var etAmount : EditText
    lateinit var btnAddExpense : Button
    lateinit var switch : SwitchCompat
    lateinit var firebaseAuth : FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var toolbar : Toolbar
    lateinit var drawerLayout : DrawerLayout
    lateinit var navigationView : NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etPlace=findViewById(R.id.etPlace)
        etAmount=findViewById(R.id.etAmount)
        btnAddExpense=findViewById(R.id.btnAddExpense)
        switch=findViewById(R.id.simpleSwitch)
        toolbar=findViewById(R.id.toolbar)
        drawerLayout=findViewById(R.id.drawerLayout)
        navigationView=findViewById(R.id.navView)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle= ActionBarDrawerToggle(this,drawerLayout,R.string.open_drawer,R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menuItemFind -> {
                    val intent= Intent(this, SearchExpenseActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuItemDashboard -> {
                    val intent= Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            return@setNavigationItemSelectedListener true
        }

        firebaseAuth=FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                btnAddExpense.setBackgroundColor(ContextCompat.getColor(this as Context,R.color.green))
            }
            else {
                btnAddExpense.setBackgroundColor(ContextCompat.getColor(this as Context,R.color.red))
            }
        }
        btnAddExpense.setOnClickListener {
            val place = etPlace.text.toString()
            val amount = etAmount.text.toString()

            if (place.isNotEmpty() && amount.isNotEmpty()) {
                btnAddExpense.isClickable = false

                val dateAndTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val date = dateAndTime.format(formatter)
                val month = dateAndTime.month.toString()
                val year = dateAndTime.year.toString()
                val sign = switch.isChecked
                val user = firebaseAuth.currentUser?.email.toString()
                val expense = Expense(place, amount, date, dateAndTime.toString(), sign)

                db.collection(user).document("years").collection(year)
                    .document("months").collection(month).document().set(expense)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT)
                            .show()
                        updateBudget(amount,sign)

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failure : $it", Toast.LENGTH_LONG).show()
                    }


            }
            else
                Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show()
        }
    }
    fun updateBudget(amount : String,sign : Boolean){
        db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").get()
            .addOnSuccessListener {
                var balance=it.get("balance").toString().toDouble().roundToInt()
                var expense = it.get("expense").toString().toDouble().roundToInt()
                var cut=amount.toDouble().roundToInt()
                if(sign) cut*=-1
                balance-=cut
                expense+=cut
                val map=HashMap<String,String>()
                map.put("balance",balance.toString())
                map.put("expense",expense.toString())
                db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").set(map,
                    SetOptions.merge())
                    .addOnSuccessListener {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Error: Couldnt update monthly expenses",Toast.LENGTH_SHORT).show()
                    }
            }
    }
    override fun onBackPressed() {
        val intent= Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        when(id){
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}