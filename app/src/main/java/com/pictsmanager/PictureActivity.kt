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
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.GlobalStatus
import com.pictsmanager.util.Huffman
import com.pictsmanager.util.RLE
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
    private val factor = 3 // sizeReduction = factor * factor // 8
    private val qualityLoss = 20 // out of 255
    private var image: Bitmap? = null
    private var imageByteArray: ByteArray = byteArrayOf()
    private val confBitmap = Bitmap.Config.ARGB_8888
    private var width = 0
    private var height = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imagePictureView = findViewById<View>(R.id.imagePictureView) as ImageView

        val filePath = intent.getStringExtra("PictureTaken")
        val file = File(filePath)
        println("file length")
        println(file.length())
        var bitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap = sizeReduction(bitmap)
        image = loseImageQuality(bitmap)
        imagePictureView!!.setImageBitmap(image)

        initButtons()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun compressImageHuffman(bitmap: Bitmap): ByteArray? {
        return null
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
            var keys: Array<Array<String>>
            if (GlobalStatus.COMPRESSION == "RLE") {
                imageByteArray = image?.let { it1 -> RLE.applyCompress(it1) }!!
                keys =
                    arrayOf<Array<String>>(arrayOf<String>(), arrayOf<String>(), arrayOf<String>())
            } else {
                imageByteArray = image?.let { it1 -> Huffman.applyCompress(it1) }!!
                keys = arrayOf<Array<String>>(Huffman.redKey, Huffman.greenKey, Huffman.blueKey)
            }
            tryAddImage(nameImg, accessReadImg, imageByteArray, width, height, keys)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sizeReduction(bitmap: Bitmap): Bitmap {
        width = bitmap.width / factor
        height = bitmap.height / factor

        val compressedImage: Bitmap = Bitmap.createBitmap(width, height, confBitmap)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = bitmap.getPixel(x * factor, y * factor)
                compressedImage.setPixel(x, y, color)
            }
        }
        return compressedImage
    }

    private fun tryAddImage(
        name: String,
        access_read: Boolean,
        byteArray: ByteArray,
        width: Int,
        height: Int,
        key: Array<Array<String>>
    ) {
        var imageModel = ImageModel(
            name = name,
            access_read = access_read,
            image = byteArray,
            owner_id = -1,
            id = -1,
            date_creation = "",
            url = "",
            width = width,
            height = height,
            red = key[0],
            green = key[1],
            blue = key[2],
            imageBM = null
        )
        val userConnexionRequest =
            GlobalService.imageService.createImage(GlobalStatus.JWT, imageModel)
        userConnexionRequest.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())
                    System.out.println(jsonObject)
                    Toast.makeText(this@PictureActivity, jsonObject.toString(), Toast.LENGTH_SHORT)
                        .show()
                } else if (response.code() == 200) {
                    Toast.makeText(this@PictureActivity, "Save Image Success", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@PictureActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    System.out.println("Untreated error")
                    Toast.makeText(this@PictureActivity, "Untreated error", Toast.LENGTH_SHORT)
                        .show()
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
        val red = color.red() * 255 - (color.red() * 255) % qualityLoss
        val green = color.green() * 255 - (color.green() * 255) % qualityLoss
        val blue = color.blue() * 255 - (color.blue() * 255) % qualityLoss
        return Color.rgb(red / 255, green / 255, blue / 255)
    }
}
