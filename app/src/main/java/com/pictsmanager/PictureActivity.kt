package com.pictsmanager

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PictureActivity : AppCompatActivity() {
    var imagePictureView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imagePictureView = findViewById<View>(R.id.imagePictureView) as ImageView

        val filePath = intent.getStringExtra("PictureTaked")
        var file : File = File(filePath)

        var bitmap : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizedBitmap = resizeBitmap(bitmap, 800, 700)

        /*
        Picasso
            .get()
            .load(filePath)
            .into(imagePictureView)
        */
        imagePictureView!!.setImageBitmap(resizedBitmap)
    }

    fun resizeBitmap(bitmap:Bitmap, width:Int, height:Int) : Bitmap {
        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }
}
