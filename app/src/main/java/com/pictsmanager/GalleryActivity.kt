package com.pictsmanager

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pictsmanager.request.model.AlbumModel
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.ui.main.AlbumFragment
import com.pictsmanager.ui.main.ImageFragment
import com.pictsmanager.util.ImageGalleryAdapter
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
            resetGridViewAndImageSelected()
        }

        go_back_button.setOnClickListener {
            val intent = Intent(this@GalleryActivity, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun resetGridViewAndImageSelected() {
    }
}