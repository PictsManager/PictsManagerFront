package com.pictsmanager

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.model.SuccessModel
import com.pictsmanager.request.service.ImageService
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_picture.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.nio.ByteBuffer


class PictureActivity : AppCompatActivity() {
    private var imagePictureView: ImageView? = null

    private var nameImg: String = System.currentTimeMillis().toString()
    private var accessReadImg: Boolean = false
    private var image: Bitmap = Bitmap.createBitmap(GlobalStatus.WIDTH, GlobalStatus.HEIGHT, Bitmap.Config.ARGB_8888)
    private val confBitmap = Bitmap.Config.ARGB_8888
    private val factor = 8 // sizeReduction = factor * factor
    private val qualityLoss = 20 // out of 255

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imagePictureView = findViewById<View>(R.id.imagePictureView) as ImageView

        val filePath = intent.getStringExtra("PictureTaken")
        var file = File(filePath)

        var bitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        var resizedImage = resizeBitmap(bitmap, GlobalStatus.WIDTH, GlobalStatus.HEIGHT)

        resizedImage = sizeReduction(resizedImage)
        resizedImage = loseImageQuality(resizedImage)

        val byteArray = bitmapToByteArray(resizedImage)

        Log.d("IMG", "${image.byteCount} ${byteArray.size}" )

        val bitmapRec = byteArrayToBitmap(byteArray)
        if (bitmapRec != null) {
            imagePictureView!!.setImageBitmap(bitmapRec)
        }
        initButtons()
    }

    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }

    private fun initButtons() {
        buttonBackCamera.setOnClickListener {
            val intent = Intent(this@PictureActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        buttonSharing.setOnClickListener {
            val intent = Intent(this@PictureActivity, SharingActivity::class.java)
            startActivity(intent)
        }

        buttonValidatePicture.setOnClickListener {
            tryAddImage(nameImg, accessReadImg, image)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sizeReduction(bitmap: Bitmap): Bitmap {
        val w = bitmap.width / factor
        val h = bitmap.height / factor

        val compressedImage: Bitmap = Bitmap.createBitmap(w, h, confBitmap)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val color = bitmap.getColor(x * factor, y * factor).toArgb()
                compressedImage.setPixel(x, y, color)
            }
        }
        return compressedImage
    }

    private fun tryAddImage(name: String, access_read: Boolean, image: Bitmap) {

        val byteArray = bitmapToByteArray(image)

        val imageModel = ImageModel(name, access_read, byteArray)
        val userConnexionRequest = ImageService.service.tryAddImage(GlobalStatus.JWT, imageModel)
        userConnexionRequest.enqueue(object : Callback<SuccessModel> {
            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                Log.d("CON", response.toString())

                if (response.code() < 400) {
                    Toast.makeText(this@PictureActivity, "Save Image Success", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@PictureActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(
                    this@PictureActivity,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val size: Int = bitmap.rowBytes * bitmap.height
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        return byteBuffer.array()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {

        val bitmap = Bitmap.createBitmap(GlobalStatus.WIDTH / factor, GlobalStatus.HEIGHT / factor, confBitmap)
        val buffer = ByteBuffer.wrap(byteArray)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loseImageQuality(bitmap: Bitmap): Bitmap {
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                bitmap.setPixel(x, y, loseColorQuality(bitmap.getColor(x, y)))
            }
        }
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loseColorQuality(color: Color): Int {
        val red = color.red() * 255 - (color.red()* 255) % qualityLoss
        val green = color.green()* 255 - (color.green()* 255) % qualityLoss
        val blue = color.blue()* 255 - (color.blue()* 255) % qualityLoss
        return Color.rgb(red /255, green/255, blue/255)
    }
}
