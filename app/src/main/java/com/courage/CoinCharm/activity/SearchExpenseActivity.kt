package com.courage.CoinCharm.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.SearchView
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.util.*

class SearchExpenseActivity : AppCompatActivity() {
    lateinit var recyclerSearch : RecyclerView
    lateinit var recyclerAdapter : ExpenseRecyclerAdapter
    lateinit var layoutManager: LinearLayoutManager
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var db : FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var autoCompleteYear : AutoCompleteTextView
    lateinit var autoCompleteMonth : AutoCompleteTextView
    lateinit var autoCompleteDay : AutoCompleteTextView
    lateinit var btnSearch : Button
    lateinit var searchView : SearchView
    lateinit var itemAdapter : ExpenseRecyclerAdapter


    val list= arrayListOf<Expense>()

    var dateComparator= Comparator<Expense> { e1, e2 ->
        e1.dateAndTime.compareTo(e2.dateAndTime)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_expense)

        recyclerSearch=findViewById(R.id.recyclerSearch)
        layoutManager= LinearLayoutManager(this)
        toolbar=findViewById(R.id.toolbar)
        drawerLayout=findViewById(R.id.drawerLayout)
        navigationView=findViewById(R.id.navView)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        autoCompleteYear=findViewById(R.id.auto_year)
        autoCompleteMonth=findViewById(R.id.auto_month)
        autoCompleteDay=findViewById(R.id.auto_day)
        btnSearch=findViewById(R.id.btnSearch)
        searchView=findViewById(R.id.searchView)

        searchView.visibility= View.GONE

        val actionBarDrawerToggle= ActionBarDrawerToggle(this,drawerLayout,R.string.open_drawer,R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        val years= arrayListOf<String>()
        setYears(years)
        val yearAdapter= ArrayAdapter(this,R.layout.drop_down_item,years)
        autoCompleteYear.setAdapter(yearAdapter)

        val months= arrayListOf<String>(
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter=ArrayAdapter(this,R.layout.drop_down_item,months)
        autoCompleteMonth.setAdapter(monthAdapter)

        val days= arrayListOf<String>()
        setDays(days)
        val dayAdapter=ArrayAdapter(this,R.layout.drop_down_item,days)
        autoCompleteDay.setAdapter(dayAdapter)

        firebaseAuth=FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                val tempList= arrayListOf<Expense>()
                val searchText=p0.toString().toLowerCase(Locale.ROOT)

                if(searchText.isNotEmpty()){

                    list.forEach {
                        if(it.place.toLowerCase().contains(searchText))
                            tempList.add(it)
                    }
                    itemAdapter.setFilteredList(tempList)
                }
                else {
                    tempList.clear()
                    tempList.addAll(list)
                    itemAdapter.setFilteredList(tempList)
                }

                return true
            }

        })

        btnSearch.setOnClickListener {
            val year=autoCompleteYear.text.toString()
            val month=autoCompleteMonth.text.toString().toUpperCase()
            val day=autoCompleteDay.text.toString()


            if(year.isEmpty() || month.isEmpty())
                Toast.makeText(this,"Year and Month are mandatory",Toast.LENGTH_SHORT).show()
            else {
                list.clear()
                val ref=db.collection(firebaseAuth.currentUser?.email.toString())
                    .document("years").collection(year).document("months").collection(month)
                if(day.isNotEmpty()) {
                    val hm = HashMap<String, String>()
                    hm["JANUARY"] = "01"
                    hm["FEBRUARY"] = "02"
                    hm["MARCH"] = "03"
                    hm["APRIL"] = "04"
                    hm["MAY"] = "05"
                    hm["JUNE"] = "06"
                    hm["JULY"] = "07"
                    hm["AUGUST"] = "08"
                    hm["SEPTEMBER"] = "09"
                    hm["OCTOBER"] = "10"
                    hm["NOVEMBER"] = "11"
                    hm["DECEMBER"] = "12"
                    val date=day + "/" + hm[month] + "/" + year
                    ref.whereEqualTo("date",date).get()
                        .addOnSuccessListener {
                            for(document in it)
                            {
                                val place=document.get("place").toString()
                                val amount=document.get("amount").toString()
                                val date=document.get("date").toString()
                                val dateAndTime=document.get("dateAndTime").toString()
                                val sign=document.get("sign") as Boolean
                                val expense=Expense(place,amount,date,dateAndTime,sign)
                                list.add(expense)
                            }
                            Collections.sort(list,dateComparator)
                            list.reverse()
                            searchView.visibility=View.VISIBLE
                            itemAdapter= ExpenseRecyclerAdapter(this,list)
                            recyclerSearch.adapter=itemAdapter
                            recyclerSearch.layoutManager=layoutManager

                            recyclerSearch.addItemDecoration(
                                DividerItemDecoration(
                                    recyclerSearch.context,
                                    (layoutManager as LinearLayoutManager).orientation
                                )
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"Couldnt receive documents",Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    ref.get()
                        .addOnSuccessListener {
                            for(document in it)
                            {
                                val place=document.get("place").toString()
                                val amount=document.get("amount").toString()
                                val date=document.get("date").toString()
                                val dateAndTime=document.get("dateAndTime").toString()
                                val sign=document.get("sign") as Boolean
                                val expense=Expense(place,amount,date,dateAndTime,sign)
                                list.add(expense)
                            }
                            Collections.sort(list,dateComparator)
                            list.reverse()
                            searchView.visibility=View.VISIBLE
                            itemAdapter= ExpenseRecyclerAdapter(this,list)
                            recyclerSearch.adapter=itemAdapter
                            recyclerSearch.layoutManager=layoutManager

                            recyclerSearch.addItemDecoration(
                                DividerItemDecoration(
                                    recyclerSearch.context,
                                    (layoutManager as LinearLayoutManager).orientation
                                )
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"Couldnt receive documents",Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

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

    }
    override fun onBackPressed() {
        val intent= Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setDays(days: ArrayList<String>) {
        for(i in 1..31)
            days.add(
                if(i<10) "0" +i.toString()
                else i.toString()
            )
    }

    private fun setYears(years: ArrayList<String>) {
        val year=LocalDate.now().year
        for(i in 2023..year)
            years.add(i.toString())
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