package com.courage.CoinCharm.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.courage.CoinCharm.adapter.ExpenseRecyclerAdapter
import com.courage.CoinCharm.model.Expense
import com.courage.notifications.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var btnAdd : FloatingActionButton
    lateinit var recyclerExpense : RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var db : FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var txtBalance : TextView
    lateinit var txtExpense : TextView
    lateinit var txtBudget : TextView
    lateinit var txtEdit : TextView
    val list= arrayListOf<Expense>()
    val channelId="Notification"
    val channelName="notifications"
    @SuppressLint("MissingPermission")
    var dateComparator= Comparator<Expense> { e1, e2 ->
        e1.dateAndTime.compareTo(e2.dateAndTime)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtBudget=findViewById(R.id.txtBudget)
        txtBalance=findViewById(R.id.txtBalance)
        txtExpense=findViewById(R.id.txtExpense)
        txtEdit=findViewById(R.id.txtEdit)

        recyclerExpense=findViewById(R.id.recyclerExpense)
        layoutManager= LinearLayoutManager(this)
        toolbar=findViewById(R.id.toolbar)
        drawerLayout=findViewById(R.id.drawerLayout)
        navigationView=findViewById(R.id.navView)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        txtEdit.setOnClickListener {
                showBudgetDialog()
        }


        val actionBarDrawerToggle=ActionBarDrawerToggle(this,drawerLayout,R.string.open_drawer,R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menuItemFind -> {
                    val intent= Intent(this, SearchExpenseActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.menuItemDashboard -> {
                    val intent= Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        db= FirebaseFirestore.getInstance()
        firebaseAuth= FirebaseAuth.getInstance()

        val dateAndTime = LocalDateTime.now()
        val year=dateAndTime.year.toString()
        val month=dateAndTime.month.toString()
        db.collection(firebaseAuth.currentUser?.email.toString())
            .document("years").collection(year).document("months").collection(month).get()
            .addOnSuccessListener {
                if(it!=null) {
                    for (document in it) {
                        val place=document.get("place").toString()
                        val amount=document.get("amount").toString()
                        val date=document.get("date").toString()
                        val dateWithTime =document.get("dateAndTime").toString()
                        val sign=(document.get("sign") ) as Boolean
                        val expense= Expense(place,amount,date, dateWithTime,sign)
                        list.add(expense)
                    }
                }
                if(list.isNotEmpty()) {
                    Collections.sort(list, dateComparator)
                    list.reverse()
                    recyclerExpense.adapter = ExpenseRecyclerAdapter(this, list)
                    recyclerExpense.layoutManager = layoutManager

                    recyclerExpense.addItemDecoration(
                        DividerItemDecoration(
                            recyclerExpense.context,
                            (layoutManager as LinearLayoutManager).orientation
                        )
                    )
                }
                else {
                    resetBudget()
                }

            }
            .addOnFailureListener {
                Toast.makeText(this,"Error : $it",Toast.LENGTH_LONG).show()
            }

        db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").get()
            .addOnSuccessListener {
                if(it.get("budget").toString()!="0.00"){
                    txtBudget.text=it.get("budget").toString()
                    txtExpense.text=it.get("expense").toString()
                    txtBalance.text= it.get("balance").toString()
                }
                else
                    firstBudgetDialog()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Some error occured",Toast.LENGTH_SHORT).show()
            }

        btnAdd=findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener {
                val intent = Intent(this, AddExpenseActivity::class.java)
                startActivity(intent)
                finish()
        }
    }
    fun resetBudget(){
        db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").get()
            .addOnSuccessListener {
                val budget=it.get("budget").toString()
                val map=HashMap<String,String>()
                map.put("budget",budget)
                map.put("balance",budget)
                map.put("expense","0.00")
                db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").set(map)
                    .addOnSuccessListener {
                        //nothing
                    }
                    .addOnFailureListener {
                        //nothing
                    }
            }
    }
    fun showBudgetDialog(){
        val box= Dialog(this)
        box.requestWindowFeature(Window.FEATURE_NO_TITLE)
        box.setCancelable(false)
        box.setContentView(R.layout.edit_budget)

        val budget= box.findViewById<EditText>(R.id.etBudget)
        val btnDone = box.findViewById<Button>(R.id.btnBudgetDone)
        val btnCancel = box.findViewById<Button>(R.id.btnBudgetCancel)

        btnDone.setOnClickListener {
            val monthlyBudget=budget.text.toString()
            if(monthlyBudget.isNotEmpty()) {
                updateBudget(monthlyBudget)
                box.dismiss()
            }
            else
                Toast.makeText(this,"Enter budget",Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            box.dismiss()
        }
        box.show()
    }
    fun firstBudgetDialog(){
        val box= Dialog(this)
        box.requestWindowFeature(Window.FEATURE_NO_TITLE)
        box.setCancelable(false)
        box.setContentView(R.layout.edit_budget)

        val budget= box.findViewById<EditText>(R.id.etBudget)
        val btnDone = box.findViewById<Button>(R.id.btnBudgetDone)
        val btnCancel = box.findViewById<Button>(R.id.btnBudgetCancel)

        btnDone.setOnClickListener {

            val monthlyBudget=budget.text.toString()
            if(monthlyBudget.isNotEmpty()) {
                updateBudget(monthlyBudget)
                box.dismiss()
            }
            else
                Toast.makeText(this,"Enter budget",Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            finish()
        }
        box.show()
    }
    fun updateBudget(budget : String){
            db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE").get()
                .addOnSuccessListener {
                        val prevBudget =it.get("budget").toString().toDouble().roundToInt()

                        val prevBalance =it.get("budget").toString().toDouble().roundToInt()

                        val cut = budget.toDouble().roundToInt() - prevBudget
                        val newBalance = prevBalance + cut
                        val map = HashMap<String, String>()
                        map.put("budget", budget)
                        map.put("balance", newBalance.toString())
                        db.collection(firebaseAuth.currentUser?.email.toString()).document("BBE")
                            .set(
                                map,
                                SetOptions.merge()
                            )
                        recreate()
                }

    }


    override fun onBackPressed() {
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