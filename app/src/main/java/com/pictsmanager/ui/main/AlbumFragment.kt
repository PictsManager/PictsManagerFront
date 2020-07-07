package com.pictsmanager.ui.main

import android.app.Dialog
import android.content.Context
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
import com.pictsmanager.R
import com.pictsmanager.request.model.AlbumModel
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.AlbumGalleryAdapter
import com.pictsmanager.util.GlobalStatus
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlbumFragment(context: Context) : Fragment() {

    var ctx: Context = context
    var albumsSelected = mutableMapOf<Int, Boolean>()
    var albums: ArrayList<AlbumModel> = ArrayList()

    lateinit var albumAdapter: AlbumGalleryAdapter
    lateinit var gridView: GridView
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view: View = inflater.inflate(R.layout.album_fragment, container, false)

        bottomNavigation = view.findViewById(R.id.album_gallery_bottom_navigation)
        gridView = view.findViewById(R.id.album_gallery_list_view) as GridView

        updateCurrentList()
        initButtons()

        return view
    }

    private fun resetGridViewAndAlbumSelected() {
        albumAdapter = AlbumGalleryAdapter(ctx, albums)
        gridView.adapter = albumAdapter

        albumsSelected.clear()
        for (i in albums) {
            albumsSelected[albums.indexOf(i)] = false
        }
        albumAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentList()
    }

    private fun initButtons() {
        gridView.setOnItemClickListener { parent, view, position, id ->
            val ISAsBool = albumsSelected[position] as Boolean
            albumsSelected[position] = !ISAsBool

            gridView.setItemChecked(position, true)

            if (albumsSelected[position] == true) {
                view.alpha = 0.5F
            } else {
                view.alpha = 1F
            }
        }

        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    showDeleteDialog(ctx)
                    true
                }
                R.id.action_share -> {
                    showShareDialog(ctx)
                    true
                }
                R.id.action_add -> {
                    showNewDialog(ctx)
                    true
                }
                R.id.action_zoom -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun showDeleteDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.album_delete_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clear_button = dialog.findViewById(R.id.delete_clear_button) as ImageButton
        val delete_button = dialog.findViewById(R.id.delete_delete_button) as Button

        clear_button.setOnClickListener {
            dialog.dismiss()
        }
        delete_button.setOnClickListener {
            val ids = getAlbumModelIdsFromAlbumSelected()
            val imageReadRequest = GlobalService.albumService.deleteAlbums(GlobalStatus.JWT, ids)
            imageReadRequest.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.code() == 400 || response.code() == 418) {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        System.out.println(jsonObject)
                        Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 200) {
                        updateCurrentList()
                        Toast.makeText(ctx, "Successfully deleted", Toast.LENGTH_SHORT).show()
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
            resetGridViewAndAlbumSelected()
            dialog.dismiss()
        }

        // change dialog size
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
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
            for (p in albumsSelected) {
                if (p.value) {
                    val position = p.key
                    val im: AlbumModel = getAlbumModelFromPosition(position)!!
                    val imageUpdateRequest = GlobalService.albumService.updateAlbum(
                        GlobalStatus.JWT,
                        im.id,
                        im.name,
                        false,
                        im.images
                    )
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.code() == 400 || response.code() == 418) {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                System.out.println(jsonObject)
                                Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            } else if (response.code() == 200) {
                                Toast.makeText(ctx, "Successfully not granted", Toast.LENGTH_SHORT)
                                    .show()
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
            for (p in albumsSelected) {
                if (p.value) {
                    val position = p.key
                    val im: AlbumModel = getAlbumModelFromPosition(position)!!
                    val imageUpdateRequest = GlobalService.albumService.updateAlbum(
                        GlobalStatus.JWT,
                        im.id,
                        im.name,
                        true,
                        null
                    )
                    imageUpdateRequest.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.code() == 400 || response.code() == 418) {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                System.out.println(jsonObject)
                                Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            } else if (response.code() == 200) {
                                Toast.makeText(ctx, "Successfully granted", Toast.LENGTH_SHORT)
                                    .show()
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

    private fun showNewDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.new_album_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        val clear_button = dialog.findViewById(R.id.new_album_clear_button) as ImageButton
        val name_edit_text = dialog.findViewById(R.id.new_album_name_edit_text) as EditText
        val validate_button = dialog.findViewById(R.id.new_album_validate_button) as Button

        clear_button.setOnClickListener {
            dialog.dismiss()
        }
        validate_button.setOnClickListener {
            val name = name_edit_text.text.toString()
            var invalid_message = ""
            if (name == "") {
                invalid_message = "Le nom ne peut être vide."
                Toast.makeText(ctx, invalid_message, Toast.LENGTH_LONG).show()
            } else {
                val imageUpdateRequest =
                    GlobalService.albumService.createAlbum(GlobalStatus.JWT, name, false)
                imageUpdateRequest.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.code() == 400 || response.code() == 418) {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            System.out.println(jsonObject)
                            Toast.makeText(ctx, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                        } else if (response.code() == 200) {
                            Toast.makeText(ctx, "Successfully create", Toast.LENGTH_SHORT).show()
                            updateCurrentList()
                        } else {
                            System.out.println("Untreated error")
                            Toast.makeText(ctx, "Untreated error", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        System.out.println(t.toString())
                        Toast.makeText(ctx, "ERROR server", Toast.LENGTH_LONG).show()
                    }
                })
            }
            dialog.dismiss()
        }

        // Change dialog size
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.40).toInt()
        dialog.window?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun getAlbumModelFromPosition(position: Int): AlbumModel? {
        for (im in albums) {
            if (albums.indexOf(im) == position) {
                return im
            }
        }
        return null
    }

    private fun getAlbumModelIdsFromAlbumSelected(): ArrayList<Long> {
        var ids: ArrayList<Long> = ArrayList()

        for (p in albumsSelected) {
            if (p.value) {
                val im = albumAdapter.getItem(p.key) as AlbumModel
                ids.add(im.id)
            }
        }
        return ids
    }

    private fun updateCurrentList() {
        val imageReadRequest = GlobalService.albumService.readAlbums(GlobalStatus.JWT, null)
        imageReadRequest.enqueue(object : Callback<ArrayList<AlbumModel>> {
            override fun onResponse(
                call: Call<ArrayList<AlbumModel>>,
                response: Response<ArrayList<AlbumModel>>
            ) {
                val body = response.body()
                body?.let {
                    albums = it
                    resetGridViewAndAlbumSelected()
                }
            }

            override fun onFailure(call: Call<ArrayList<AlbumModel>>, t: Throwable) {
                Log.d("ERR", t.toString())
                Toast.makeText(ctx, "ERROR server: read", Toast.LENGTH_LONG).show()
            }
        })
    }
}