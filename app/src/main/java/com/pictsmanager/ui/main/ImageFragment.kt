package com.pictsmanager.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
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
import com.pictsmanager.util.ImageGalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.image_fragment.*

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
        images.add(ImageModel(1, "Salameche", R.drawable.salameche, 0, false))
        images.add(ImageModel(2, "Damso", R.drawable.damso, 0, false))
        images.add(ImageModel(3, "Voltaire", R.drawable.voltaire, 0, false))
        images.add(ImageModel(4, "Johnny", R.drawable.johnny, 0, false))
        images.add(ImageModel(5, "Jack", R.drawable.jack, 0, false))
        images.add(ImageModel(6, "Aragorn", R.drawable.aragorn, 0, false))
        /*images.add(ImageModel(5, "Salameche 5", R.drawable.salameche, 0, false))
        images.add(ImageModel(6, "Salameche 6", R.drawable.salameche, 0, false))
        images.add(ImageModel(7, "Salameche 7", R.drawable.salameche, 0, false))
        images.add(ImageModel(8, "Salameche 8", R.drawable.salameche, 0, false))
        images.add(ImageModel(9, "Salameche 9", R.drawable.salameche, 0, false))
        images.add(ImageModel(10, "Salameche 10", R.drawable.salameche, 0, false))
        images.add(ImageModel(11, "Salameche 11", R.drawable.salameche, 0, false))
        images.add(ImageModel(12, "Salameche 12", R.drawable.salameche, 0, false))
        images.add(ImageModel(13, "Salameche 13", R.drawable.salameche, 0, false))
        images.add(ImageModel(14, "Salameche 14", R.drawable.salameche, 0, false))
        images.add(ImageModel(15, "Salameche 15", R.drawable.salameche, 0, false))
        images.add(ImageModel(16, "Salameche 16", R.drawable.salameche, 0, false))
        images.add(ImageModel(17, "Salameche 17", R.drawable.salameche, 0, false))
        images.add(ImageModel(18, "Salameche 18", R.drawable.salameche, 0, false))
        images.add(ImageModel(19, "Salameche 19", R.drawable.salameche, 0, false))
        images.add(ImageModel(20, "Salameche 20", R.drawable.salameche, 0, false))
        images.add(ImageModel(21, "Salameche 21", R.drawable.salameche, 0, false))*/

        resetGridViewAndImageSelected()

        initButtons()

        return view
    }

    //TODO: When anew user see this view, images list is updated
    override fun onResume() {
        super.onResume()
        //TODO: Api call completing Images array

        if (images != null) {
            //TODO: rebuild adapter with refreshed albums, set adapter ti gridview
        }
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
            //TODO: Api call performing multiple suppression
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

    private fun showAddDialog(context: Context) {
        //TODO: Api call get Albums
        var albums: ArrayList<AlbumModel> = ArrayList()
        albums.add(AlbumModel(1, "album 1", ArrayList<ImageModel>(), 1, false))
        albums.add(AlbumModel(2, "album 2", ArrayList<ImageModel>(), 1, false))
        albums.add(AlbumModel(3, "album 3", ArrayList<ImageModel>(), 1, false))
        albums.add(AlbumModel(4, "album 4", ArrayList<ImageModel>(), 1, false))
        var albums1 = albums //TODO: Replace by Api result
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
            //TODO: Api call post images id with name album param
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
}