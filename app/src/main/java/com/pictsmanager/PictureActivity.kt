package com.pictsmanager

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_picture.*
import java.io.File
import java.io.IOException

class PictureActivity : AppCompatActivity() {
    var imagePictureView: ImageView? = null

    companion object {
        private val ORIENTATION = SparseIntArray()

        init {
            ORIENTATION.append(Surface.ROTATION_0, 90)
            ORIENTATION.append(Surface.ROTATION_90, 0)
            ORIENTATION.append(Surface.ROTATION_180, 270)
            ORIENTATION.append(Surface.ROTATION_270, 180)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imagePictureView = findViewById<View>(R.id.imagePictureView) as ImageView


        val filePath = intent.getStringExtra("PictureTaked")
        var file : File = File(filePath)
        Log.d("FILE :: ", filePath)

        var bitmap : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        imagePictureView!!.setImageBitmap(bitmap)

        /*if (bundle != null) {
            val resId = bundle.getInt("PictureTaked")
            imagePictureView?.setImageResource(resId)
        }*/
    }
}
