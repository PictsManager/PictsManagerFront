package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pictsmanager.util.TabAdapter
import kotlinx.android.synthetic.main.activity_gallery.*


class GalleryActivity : AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.viewpager)

        val tabAdapter = TabAdapter(this, supportFragmentManager)
        viewPager!!.adapter = tabAdapter

        tabLayout.setupWithViewPager(viewPager)
        initButtons()
    }

    private fun initButtons() {
        cancel_button.setOnClickListener {
            resetGridViewSelected()
        }

        go_back_button.setOnClickListener {
            val intent = Intent(this@GalleryActivity, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun resetGridViewSelected() {
        finish()
        startActivity(intent)
        Toast.makeText(this, "RESET", Toast.LENGTH_SHORT).show()
    }
}