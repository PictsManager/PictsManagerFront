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
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_search.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            var order : Boolean
            if (searchInput.text != null) {
                if (spDateOrder.selectedItemPosition == 0) {
                    // From the oldest"
                    order = true
                } else {
                    // From the most recent
                    order = false
                }
                requestForSearching(searchInput.text.toString(), order)
                Toast.makeText(this,
                    "You selected ${searchInput.text} ${order}",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "No search",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestForSearching(tags: String, order: Boolean) {
        val imageReadRequest = GlobalService.imageService.searchImage(GlobalStatus.JWT, tags, true, order)
        /*imageReadRequest.enqueue(object : Callback<ArrayList<ImageModel>> {
            override fun onResponse(
                call: Call<ArrayList<ImageModel>>,
                response: Response<ArrayList<ImageModel>>
            ) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())

                    System.out.println(jsonObject)
                    Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 200) {
                    val body = response.body()
                    body?.let {
                        images = it
                        completeImageModelWithDecompressBitmap(images)
                        resetGridViewAndImageSelected()
                        Toast.makeText(ctx, "Successful update", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    System.out.println("Untreated error")
                    Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArrayList<ImageModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(ctx, "ERROR server: read", Toast.LENGTH_LONG).show()
            }
        })*/
    }
}