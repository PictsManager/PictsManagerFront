package com.pictsmanager

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.ui.main.ImageFragment
import com.pictsmanager.util.GlobalStatus
import com.pictsmanager.util.Huffman
import com.pictsmanager.util.ImageGalleryAdapter
import com.pictsmanager.util.RLE
import kotlinx.android.synthetic.main.activity_connexion.*
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_search.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    var images: ArrayList<ImageModel> = ArrayList()
    var selfOption : Boolean = false
    lateinit var gridView: GridView
    lateinit var imageAdapter: ImageGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        gridView =findViewById(R.id.searchGridView) as GridView

        spDateOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                /*Toast.makeText(this@SearchActivity,
                    "You selected ${adapterView?.getItemAtPosition(position).toString()}",
                    Toast.LENGTH_LONG).show()*/
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
            var order : Boolean
            if (searchInput.text != null) {
                order = spDateOrder.selectedItemPosition != 0

                requestForSearching(adaptTagsString(searchInput.text.toString()), order)

            } else {
                Toast.makeText(this,
                    "No search",
                    Toast.LENGTH_SHORT).show()
            }
        }

        switchSelf.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                textView3.setText("S'inclure")
                selfOption = true
            } else {
                textView3.setText("Privée")
                selfOption = false
            }
        }
    }

    private fun adaptTagsString(tags: String) : String {
        var result =  tags.replace(", ", ",", ignoreCase = false)
        return result.replace(" ,", ",", ignoreCase = false)
    }

    private fun requestForSearching(tags: String, order: Boolean) {
        val imageReadRequest = GlobalService.imageService.searchImage(GlobalStatus.JWT, tags, selfOption, order)
        Log.d("TAGS ", tags + " " + selfOption)
        imageReadRequest.enqueue(object : Callback<ArrayList<ImageModel>> {
            override fun onResponse(
                call: Call<ArrayList<ImageModel>>,
                response: Response<ArrayList<ImageModel>>
            ) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())

                    System.out.println(jsonObject)
                } else if (response.code() == 200) {
                    val body = response.body()
                    body?.let {
                        images = it
                        completeImageModelWithDecompressBitmap(images)
                        resetGridViewAndImageSelected()
                    }
                    System.out.println(response.body())
                } else {
                    System.out.println("Untreated error")
                }
            }

            override fun onFailure(call: Call<ArrayList<ImageModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
            }
        })
    }

    private fun completeImageModelWithDecompressBitmap(toComplete: ArrayList<ImageModel>) {
        if (GlobalStatus.COMPRESSION == "RLE") {
            for (im in toComplete) {
                val bmp: Bitmap = RLE.applyDecompress(im.image, im.width, im.height)
                im.imageBM = bmp
            }
        } else {
            for (im in toComplete) {
                val keys = arrayOf<Array<String>>(im.red, im.green, im.blue)
                val bmp: Bitmap = Huffman.applyDecompress(im.image, im.width, im.height, keys)
                im.imageBM = bmp
            }
        }
    }

    private fun resetGridViewAndImageSelected() {
        imageAdapter = ImageGalleryAdapter(this, images)
        gridView.adapter = imageAdapter

        imageAdapter.notifyDataSetChanged()
    }
}