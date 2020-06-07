package com.pictsmanager.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pictsmanager.HomeActivity
import com.pictsmanager.R
import com.pictsmanager.request.model.AlbumModel
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.model.SuccessModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.request.service.UserService
import com.pictsmanager.util.GlobalStatus
import com.pictsmanager.util.ImageGalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.image_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageFragment(context: Context): Fragment(){

    var ctx: Context = context
    var imagesSelected = mutableMapOf<Int, Boolean>()
    var images: ArrayList<ImageModel> = ArrayList()
    var albumSelected: String = ""

    lateinit var imageAdapter: ImageGalleryAdapter
    lateinit var gridView: GridView
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        var view: View = inflater.inflate(R.layout.image_fragment, container, false)

        bottomNavigation = view.findViewById(R.id.image_gallery_bottom_navigation)
        gridView = view.findViewById(R.id.image_gallery_list_view) as GridView

        updateCurrentList()
        initButtons()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateCurrentList()
    }

    private fun initButtons() {
        gridView.setOnItemClickListener { parent, view, position, id ->
            //val image = images.get(position)
            val ISAsBool = imagesSelected[position] as Boolean
            imagesSelected[position] = ! ISAsBool

            gridView.setItemChecked(position, true)

            if (imagesSelected[position] == true) {
                view.alpha = 0.5F
            } else {
                view.alpha = 1F
            }
        }

        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.action_delete -> {
                    showDeleteDialog(ctx)
                    true
                }
                R.id.action_share -> {
                    showShareDialog(ctx)
                    true
                }
                R.id.action_add -> {
                    showAddDialog(ctx)
                    true
                }
                else -> false
            }
        }
    }

    private fun resetGridViewAndImageSelected() {
        imageAdapter = ImageGalleryAdapter(ctx, images)
        gridView.adapter = imageAdapter

        imagesSelected.clear()
        for (i in images) {
            imagesSelected[images.indexOf(i)] = false
        }
        imageAdapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_delete_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clear_button = dialog.findViewById(R.id.delete_clear_button) as ImageButton
        val delete_button = dialog.findViewById(R.id.delete_delete_button) as Button

        clear_button.setOnClickListener {
            dialog.dismiss()
        }
        delete_button.setOnClickListener {
            val ids = getImageModelIdsFromImageSelected()
            val imageReadRequest = GlobalService.imageService.deleteImages(ids)
            imageReadRequest.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    val body = response.body()
                    body?.let {
                        Toast.makeText(ctx, it.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.d("ERR", t.toString())
                    Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                }
            })
            for (p in imagesSelected) {
                if (p.value) {
                    images.remove(imageAdapter.getItem(p.key))
                }
            }
            resetGridViewAndImageSelected()
            dialog.dismiss()
        }

        // change dialog size
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
        dialog.getWindow()?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showShareDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.share_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clear_button = dialog.findViewById(R.id.share_clear_button) as ImageButton
        val not_granted_button = dialog.findViewById(R.id.share_not_granted_button) as Button
        val granted_button = dialog.findViewById(R.id.share_granted_button) as Button

        clear_button.setOnClickListener {
            dialog.dismiss()
        }
        not_granted_button.setOnClickListener {
            for (p in imagesSelected) {
                if (p.value) {
                    val position = p.key
                    val im: ImageModel = getImageModelFromPosition(position)!!
                    val imageUpdateRequest = GlobalService.imageService.updateImage(im.id, im.name, false)
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            val body = response.body()
                            body?.let {
                                Toast.makeText(ctx, it.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            Log.d("ERR", t.toString())
                            Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
            dialog.dismiss()
        }
        granted_button.setOnClickListener {
            for (p in imagesSelected) {
                if (p.value) {
                    val position = p.key
                    val im: ImageModel = getImageModelFromPosition(position)!!
                    val imageUpdateRequest = GlobalService.imageService.updateImage(im.id, im.name, true)
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            val body = response.body()
                            body?.let {
                                Toast.makeText(ctx, it.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            Log.d("ERR", t.toString())
                            Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
            dialog.dismiss()
        }

        // Change dialog size
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
        dialog.getWindow()?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showAddDialog(context: Context) {
        val albumReadRequest = GlobalService.albumService.readAlbums(null)
        var resp: Response<ArrayList<AlbumModel>> = albumReadRequest.execute()
        var albums1: ArrayList<AlbumModel> = ArrayList()

        if (resp.isSuccessful) {
            albums1 = albumReadRequest.execute().body()!!
        }

        /*albumReadRequest.enqueue(object : Callback<ArrayList<AlbumModel>> {
            override fun onResponse(call: Call<ArrayList<AlbumModel>>, response: Response<ArrayList<AlbumModel>>) {
                val body = response.body()
                body?.let {
                    Toast.makeText(ctx, it.toString(), Toast.LENGTH_LONG).show()
                    albums1 = it
                }
            }
            override fun onFailure(call: Call<ArrayList<AlbumModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
            }
        })*/

        var album_labels: ArrayList<String> = ArrayList()

        for (i in albums1) {
            album_labels.add(i.name)
        }

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clear_button = dialog.findViewById(R.id.add_clear_button) as ImageButton
        val album_spinner = dialog.findViewById(R.id.add_album_spinner) as Spinner
        val add_button = dialog.findViewById(R.id.add_add_button) as Button

        var arrAdapter = ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, album_labels)
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        album_spinner.adapter = arrAdapter

        clear_button.setOnClickListener {
            dialog.dismiss()
        }
        add_button.setOnClickListener {
            val albumName: String = album_spinner.selectedItem as String
            var selectedAlbum: AlbumModel = getAlbumModelFromName(albums1, albumName)!!
            var newIds = getImageModelIdsFromImageSelected()
            for (id in newIds) {
                selectedAlbum.images.add(id)
            }
            var albumUpdateRequest = GlobalService.albumService.updateAlbum(selectedAlbum.id, selectedAlbum.name, selectedAlbum.access_read, selectedAlbum.images)
            albumUpdateRequest.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    val body = response.body()
                    body?.let {
                        Toast.makeText(ctx, it.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.d("ERR", t.toString())
                    Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                }
            })
            dialog.dismiss()
        }
        album_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                albumSelected = album_labels.get(position)
            }
        }

        // Change dialog size
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.40).toInt()
        dialog.getWindow()?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun readImages() {
        val imageReadRequest = GlobalService.imageService.readImages(null)
        imageReadRequest.enqueue(object : Callback<ArrayList<ImageModel>> {
            override fun onResponse(call: Call<ArrayList<ImageModel>>, response: Response<ArrayList<ImageModel>>) {
                val body = response.body()
                body?.let {
                }
            }

            override fun onFailure(call: Call<ArrayList<ImageModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
            }
        })
    }

    private fun getImageModelFromPosition(position: Int): ImageModel? {
        for (im in images) {
            if (images.indexOf(im) == position) {
                return im
            }
        }
        return null
    }

    private fun getAlbumModelFromName(albums: ArrayList<AlbumModel>, name: String): AlbumModel? {
        for (alb in albums) {
            if (alb.name == name) {
                return alb
            }
        }
        return null
    }

    private fun getImageModelIdsFromImageSelected(): ArrayList<Long> {
        var ids: ArrayList<Long> = ArrayList()

        for (p in imagesSelected) {
            if (p.value) {
                val im = imageAdapter.getItem(p.key) as ImageModel
                ids.add(im.id)
            }
        }
        return ids
    }

    private fun updateCurrentList() {
        val imageReadRequest = GlobalService.imageService.readImages(null)
        imageReadRequest.enqueue(object : Callback<ArrayList<ImageModel>> {
            override fun onResponse(call: Call<ArrayList<ImageModel>>, response: Response<ArrayList<ImageModel>>) {
                val body = response.body()
                body?.let {
                    images = it
                    resetGridViewAndImageSelected()
                }
            }
            override fun onFailure(call: Call<ArrayList<ImageModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(ctx, "ERROR server: read", Toast.LENGTH_LONG).show()
            }
        })
    }
}