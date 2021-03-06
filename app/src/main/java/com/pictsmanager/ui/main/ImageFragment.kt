package com.pictsmanager.ui.main

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pictsmanager.R
import com.pictsmanager.request.model.AlbumModel
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.GlobalStatus
import com.pictsmanager.util.Huffman
import com.pictsmanager.util.ImageGalleryAdapter
import com.pictsmanager.util.RLE
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ImageFragment(context: Context) : Fragment() {

    var ctx: Context = context
    var imagesSelected = mutableMapOf<Int, Boolean>()
    var images: ArrayList<ImageModel> = ArrayList()
    var albumSelected: String = ""

    lateinit var imageAdapter: ImageGalleryAdapter
    lateinit var gridView: GridView
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
            val ISAsBool = imagesSelected[position] as Boolean
            imagesSelected[position] = !ISAsBool

            gridView.setItemChecked(position, true)

            if (imagesSelected[position] == true) {
                view.alpha = 0.5F
            } else {
                view.alpha = 1F
            }
        }

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    showDeleteDialog(ctx)
                    true
                }
                R.id.action_share -> {
                    showShareDialog(ctx)
                    true
                }
                R.id.action_tags -> {
                    showTagsDialog(ctx)
                    true
                }
                R.id.action_add -> {
                    showAddDialog(ctx)
                    true
                }
                R.id.action_zoom -> {
                    showZoomView(ctx)
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
            val imageReadRequest = GlobalService.imageService.deleteImages(GlobalStatus.JWT, ids)
            imageReadRequest.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.code() == 400 || response.code() == 418) {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        System.out.println(jsonObject)
                        Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 200) {
                        val body = response.body()
                        body?.let {
                            updateCurrentList()
                            Toast.makeText(ctx, "Successful deleted", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        System.out.println("Untreated error")
                        Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.d("ERR", t.toString())
                    Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                }
            })
            dialog.dismiss()
        }

        // change dialog size
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
        dialog.window?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showZoomView(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_zoom)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        var imageZoom = dialog.findViewById(R.id.imageView) as ImageView
        val clear_button = dialog.findViewById(R.id.zoom_clear_button) as ImageButton

        clear_button.setOnClickListener {
            dialog.dismiss()
        }

        for (p in imagesSelected) {
            if (p.value) {
                val position = p.key
                val im: ImageModel = getImageModelFromPosition(position)!!

                imageZoom.setImageBitmap(im.imageBM)

            }
        }

        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 1).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.9).toInt()
        dialog.window?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showTagsDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_tags_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clearButton = dialog.findViewById(R.id.add_tags_clear_button) as ImageButton
        val addTagsEditText = dialog.findViewById(R.id.add_tags_edit_text) as EditText
        val addTagsEditText2 = dialog.findViewById(R.id.add_tags_edit_text2) as EditText
        val addTagsEditText3 = dialog.findViewById(R.id.add_tags_edit_text3) as EditText
        val validateTags = dialog.findViewById(R.id.add_tags_validate_button) as Button

        clearButton.setOnClickListener {
            dialog.dismiss()
        }

        validateTags.setOnClickListener {

        }

        listOf(addTagsEditText, addTagsEditText2, addTagsEditText3).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
/*                    var text = s.toString().replace(" ", "")
                    text = text.replace("\n", "")
                    text = text.replace(",", "")
                    it.setText(text)*/
                }
            })
        }

        validateTags.setOnClickListener {
            val tags =
                addTagsEditText.text.toString() + "," + addTagsEditText2.text.toString() + "," + addTagsEditText3.text.toString()
            for (p in imagesSelected) {
                if (p.value) {
                    val position = p.key
                    val im: ImageModel = getImageModelFromPosition(position)!!
                    val imageUpdateRequest = GlobalService.imageService.updateImage(
                        GlobalStatus.JWT,
                        im.id,
                        im.name,
                        tags
                    )
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.code() == 400 || response.code() == 418) {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                System.out.println(jsonObject)
                                Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            } else if (response.code() == 200) {
                                val body = response.body()
                                body?.let {
                                    resetGridViewAndImageSelected()
                                    Toast.makeText(ctx, "Successful ungranted", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                System.out.println("Untreated error")
                                Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
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
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.60).toInt()
        dialog.window?.setLayout(dialogWidth, dialogHeight)
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
                    val imageUpdateRequest = GlobalService.imageService.updateImage(
                        GlobalStatus.JWT,
                        im.id,
                        im.name,
                        false
                    )
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.code() == 400 || response.code() == 418) {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                System.out.println(jsonObject)
                                Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            } else if (response.code() == 200) {
                                val body = response.body()
                                body?.let {
                                    resetGridViewAndImageSelected()
                                    Toast.makeText(ctx, "Successful ungranted", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                System.out.println("Untreated error")
                                Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
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
                    val imageUpdateRequest = GlobalService.imageService.updateImage(
                        GlobalStatus.JWT,
                        im.id,
                        im.name,
                        true
                    )
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.code() == 400 || response.code() == 418) {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                System.out.println(jsonObject)
                                Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            } else if (response.code() == 200) {
                                val body = response.body()
                                body?.let {
                                    resetGridViewAndImageSelected()
                                    Toast.makeText(ctx, "Successful granted", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                System.out.println("Untreated error")
                                Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
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
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
        dialog.window?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showAddDialog(context: Context) {
        val albumReadRequest = GlobalService.albumService.readAlbums(GlobalStatus.JWT, null)
        albumReadRequest.enqueue(object : Callback<ArrayList<AlbumModel>> {
            override fun onResponse(
                call: Call<ArrayList<AlbumModel>>,
                response: Response<ArrayList<AlbumModel>>
            ) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())

                    System.out.println(jsonObject)
                    Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT).show()

                } else if (response.code() == 200) {
                    val body = response.body()

                    body?.let {
                        val readAlbum = it
                        var album_labels: ArrayList<String> = ArrayList()

                        for (i in readAlbum) {
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

                        var arrAdapter = ArrayAdapter<String>(
                            ctx,
                            android.R.layout.simple_spinner_dropdown_item,
                            album_labels
                        )
                        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        album_spinner.adapter = arrAdapter

                        clear_button.setOnClickListener {
                            dialog.dismiss()
                        }

                        add_button.setOnClickListener {
                            val albumName: String = album_spinner.selectedItem as String
                            val selectedAlbum: AlbumModel =
                                getAlbumModelFromName(readAlbum, albumName)!!
                            val newIds = getImageModelIdsFromImageSelected()

                            selectedAlbum.images = arrayListOf<Long>()
                            for (id in newIds) {
                                selectedAlbum.images.add(id)
                            }

                            var albumUpdateRequest = GlobalService.albumService.updateAlbum(
                                GlobalStatus.JWT,
                                selectedAlbum.id,
                                selectedAlbum.name,
                                selectedAlbum.access_read,
                                selectedAlbum.images
                            )
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

                        album_spinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                }

                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    albumSelected = album_labels.get(position)
                                }
                            }

                        // Change dialog size
                        val displayMetrics: DisplayMetrics =
                            context.resources.displayMetrics
                        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
                        val dialogHeight = (displayMetrics.heightPixels * 0.40).toInt()
                        dialog.window?.setLayout(dialogWidth, dialogHeight)
                        dialog.show()
                    }
                } else {
                    System.out.println("Untreated error")
                    Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArrayList<AlbumModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
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
        val imageReadRequest = GlobalService.imageService.readImages(GlobalStatus.JWT, null)
        imageReadRequest.enqueue(object : Callback<ArrayList<ImageModel>> {
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
}