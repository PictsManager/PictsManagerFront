package com.pictsmanager.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.pictsmanager.R
import com.pictsmanager.request.model.ImageModel

class ImageGalleryAdapter(var ctx: Context, var images: ArrayList<ImageModel>): BaseAdapter() {

    override fun getCount(): Int {
        return images.count()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return images.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        var viewHolder: ViewHolder

        if (convertView == null) {
            var layout = LayoutInflater.from(ctx)
            view = layout.inflate(R.layout.image_item, parent, false)

            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val image: ImageModel = getItem(position) as ImageModel
        val bmp: Bitmap = GlobalStatus.decompressImageRLE(image.image)
        //viewHolder.imageViewImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, 200, false))
        viewHolder.imageViewImage.setImageBitmap(bmp)

        return view as View
    }

    private class ViewHolder(row: View?) {
        //var textViewImage: TextView
        var imageViewImage: ImageView

        init {
            //this.textViewImage = row?.findViewById(R.id.item_text_view) as TextView
            this.imageViewImage = row?.findViewById(R.id.item_image_image_view) as ImageView
        }
    }
}