package com.pictsmanager.util

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.pictsmanager.R
import com.pictsmanager.request.model.AlbumModel
import com.pictsmanager.request.model.ImageModel

class AlbumGalleryAdapter(var ctx: Context, var albums: ArrayList<AlbumModel>): BaseAdapter() {

    override fun getCount(): Int {
        return albums.count()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return albums.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        var viewHolder: ViewHolder

        if (convertView == null) {
            var layout = LayoutInflater.from(ctx)
            view = layout.inflate(R.layout.album_item, parent, false)

            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var album: AlbumModel = getItem(position) as AlbumModel
        val idResourceImageAlbum = ctx.resources.getIdentifier("album_background", "drawable", ctx.packageName)

        viewHolder.textViewAlbum.text = album.name
        viewHolder.imageViewAlbum.setImageResource(idResourceImageAlbum)

        return view as View
    }

    private class ViewHolder(row: View?) {
        var textViewAlbum: TextView
        var imageViewAlbum: ImageView

        init {
            this.textViewAlbum = row?.findViewById(R.id.item_album_text_view) as TextView
            this.imageViewAlbum = row?.findViewById(R.id.item_album_image_view) as ImageView
        }
    }
}