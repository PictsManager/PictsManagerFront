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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_picture.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PictureActivity : AppCompatActivity() {
    private var imagePictureView: ImageView? = null

    private var nameImg: String = System.currentTimeMillis().toString()
    private var accessReadImg: Boolean = false
    private val factor = 2 // sizeReduction = factor * factor // 8
    private val qualityLoss = 20 // out of 255
    private var image: Bitmap? = null
    private var imageByteArray: ByteArray = byteArrayOf()
    private val confBitmap = Bitmap.Config.ARGB_8888

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imagePictureView = findViewById<View>(R.id.imagePictureView) as ImageView

        val filePath = intent.getStringExtra("PictureTaken")
        var file = File(filePath)

        var bitmap : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap = sizeReduction(bitmap)
        image = loseImageQuality(bitmap)
        imagePictureView!!.setImageBitmap(image)
        initButtons()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun compressImageRLE(resizedBitmap: Bitmap) : ByteArray {
        var array = byteArrayOf()
        val redArray = ArrayList<Int>()
        val greenArray = ArrayList<Int>()
        val blueArray = ArrayList<Int>()

        for (y in 0 until resizedBitmap.height) {
            for (x in 0 until resizedBitmap.width) {
                redArray.add(Color.red(resizedBitmap.getPixel(x, y)))
                greenArray.add(Color.green(resizedBitmap.getPixel(x, y)))
                blueArray.add(Color.blue(resizedBitmap.getPixel(x, y)))
            }
        }
        array = appendColorInByteArray(redArray, array)
        array = appendColorInByteArray(greenArray, array)
        array = appendColorInByteArray(blueArray, array)
        return array
    }

    private fun appendColorInByteArray(colorArray : ArrayList<Int>, array : ByteArray) : ByteArray{
        var i: Int = 1
        var byteArray = array
        var end = false
        var a = 0
        for (x in 0 until colorArray.size) {
            if (((x + 1) < colorArray.size) && (colorArray[x] == colorArray[x + 1]) && i < 125) {
                i += 1
                a = x
                end = true
            } else {
                end = false
                byteArray += i.toByte()
                byteArray += colorArray[x].toByte()
                i = 1
            }
        }
        if (end) {
            byteArray += i.toByte()
            byteArray += colorArray[a].toByte()
        }

        return byteArray
    }


    @RequiresApi(Build.VERSION_CODES.Q)
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
            imageByteArray = image?.let { it1 -> compressImageRLE(it1) }!!
            tryAddImage(nameImg, accessReadImg, imageByteArray)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sizeReduction(bitmap: Bitmap): Bitmap {
        GlobalStatus.IMG_H = bitmap.height / factor
        GlobalStatus.IMG_W = bitmap.width / factor
        val w = GlobalStatus.IMG_W
        val h = GlobalStatus.IMG_H

        val compressedImage: Bitmap = Bitmap.createBitmap(w, h, confBitmap)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val color = bitmap.getPixel(x * factor, y * factor)
                compressedImage.setPixel(x, y, color)
            }
        }
        return compressedImage
    }

    private fun tryAddImage(name: String, access_read: Boolean, byteArray: ByteArray) {
        var imageModel = ImageModel(name = name, access_read = access_read, image = byteArray, owner_id = -1, id = -1, date_creation = "", url = "")
        val userConnexionRequest = GlobalService.imageService.createImage(GlobalStatus.JWT, imageModel)
        userConnexionRequest.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())
                    System.out.println(jsonObject)
                    Toast.makeText(this@PictureActivity, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 200) {
                    Toast.makeText(this@PictureActivity, "Save Image Success", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@PictureActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    System.out.println("Untreated error")
                    Toast.makeText(this@PictureActivity, "Untreated error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(
                    this@PictureActivity,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loseImageQuality(bitmap: Bitmap): Bitmap {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until newBitmap.width) {
            for (y in 0 until newBitmap.height) {
                val c = loseColorQuality(Color.valueOf(newBitmap.getPixel(x, y)))
                newBitmap.setPixel(x, y, c)
            }
        }
        return newBitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loseColorQuality(color: Color): Int {
        val red = color.red() * 255 - (color.red()* 255) % qualityLoss
        val green = color.green()* 255 - (color.green()* 255) % qualityLoss
        val blue = color.blue()* 255 - (color.blue()* 255) % qualityLoss
        return Color.rgb(red /255, green/255, blue/255)
    }
}
