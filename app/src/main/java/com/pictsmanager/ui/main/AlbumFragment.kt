package com.pictsmanager.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
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
import com.pictsmanager.util.AlbumGalleryAdapter

class AlbumFragment(context: Context): Fragment() {
    var ctx: Context = context

    var albumsSelected = mutableMapOf<Int, Boolean>()
    var albums: ArrayList<AlbumModel> = ArrayList()
    lateinit var albumAdapter: AlbumGalleryAdapter
    lateinit var gridView: GridView
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        var view: View = inflater.inflate(R.layout.album_fragment, container, false)

        bottomNavigation = view.findViewById(R.id.album_gallery_bottom_navigation)

        gridView = view.findViewById(R.id.album_gallery_list_view) as GridView

        var images1: ArrayList<ImageModel> = ArrayList()
        images1.add(ImageModel(1, "Salameche 1", R.drawable.salameche, 0, false))
        images1.add(ImageModel(2, "Salameche 2", R.drawable.salameche, 0, false))
        images1.add(ImageModel(3, "Salameche 3", R.drawable.salameche, 0, false))
        images1.add(ImageModel(4, "Salameche 4", R.drawable.salameche, 0, false))

        var images2: ArrayList<ImageModel> = ArrayList()
        images2.add(ImageModel(5, "Salameche 5", R.drawable.salameche, 0, false))
        images2.add(ImageModel(6, "Salameche 6", R.drawable.salameche, 0, false))
        images2.add(ImageModel(7, "Salameche 7", R.drawable.salameche, 0, false))
        images2.add(ImageModel(8, "Salameche 8", R.drawable.salameche, 0, false))

        var images3: ArrayList<ImageModel> = ArrayList()
        images3.add(ImageModel(9, "Salameche 9", R.drawable.salameche, 0, false))
        images3.add(ImageModel(10, "Salameche 10", R.drawable.salameche, 0, false))
        images3.add(ImageModel(11, "Salameche 11", R.drawable.salameche, 0, false))
        images3.add(ImageModel(12, "Salameche 12", R.drawable.salameche, 0, false))

        albums.add(AlbumModel(1, "my album 1", images1, 1, false))
        albums.add(AlbumModel(2, "my album 2", images2, 1, false))
        albums.add(AlbumModel(3, "my album 3", images2, 1, false))

        resetGridViewAndAlbumSelected()

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

    //TODO: When anew user see this view, images list is updated
    override fun onResume() {
        super.onResume()
        //TODO: Api call completing Images array

        if (albums != null) {
            //TODO: rebuild adapter with refreshed albums, set adapter ti gridview
        }
    }

    private fun initButtons() {
        gridView.setOnItemClickListener { parent, view, position, id ->
            val ISAsBool = albumsSelected[position] as Boolean
            albumsSelected[position] = ! ISAsBool

            gridView.setItemChecked(position, true)

            if (albumsSelected[position] == true) {
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
                    showNewDialog(ctx)
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
            //TODO: Api call performing multiple suppression
            for (p in albumsSelected) {
                if (p.value) {
                    albums.remove(albumAdapter.getItem(p.key))
                }
            }
            resetGridViewAndAlbumSelected()
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
            //TODO: Api call granted images list access_read
            dialog.dismiss()
        }
        granted_button.setOnClickListener {
            //TODO: Api call not granted images list access_read
            dialog.dismiss()
        }

        // Change dialog size
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.30).toInt()
        dialog.getWindow()?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }

    private fun showNewDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_dialog)
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
            } else {
                //TODO: Api call post images id with name album param
            }
            dialog.dismiss()
        }

        // Change dialog size
        val displayMetrics: DisplayMetrics = context.getResources().getDisplayMetrics()
        val dialogWidth = (displayMetrics.widthPixels * 0.80).toInt()
        val dialogHeight = (displayMetrics.heightPixels * 0.40).toInt()
        dialog.getWindow()?.setLayout(dialogWidth, dialogHeight)
        dialog.show()
    }
}