package com.pictsmanager

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        var searchValue = findViewById(R.id.searchInput) as EditText

        spDateOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@SearchActivity,
                    "You selected ${adapterView?.getItemAtPosition(position).toString()}",
                    Toast.LENGTH_LONG).show()
            }

        }
        initButtons()
    }

    private fun initButtons() {
        buttonBackCamera2.setOnClickListener {
            val intent = Intent(this@SearchActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        searchSubmit.setOnClickListener {
            var date : String
            if (searchInput.text != null) {
                if (spDateOrder.selectedItemPosition == 0) {
                    date = "from the oldest"
                } else {
                    date = "from the most recent"
                }
                Toast.makeText(this,
                    "You selected ${searchInput.text} ${date}",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "No search",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}