package com.pictsmanager.util

import android.graphics.Bitmap
import android.graphics.Color

class GlobalStatus {
    companion object {
        const val API_URL = "http://192.168.1.15:5005/"
        var JWT = ""
        var WIDTH: Int = 480
        var HEIGHT: Int = 640
        var IMG_W: Int = 480
        var IMG_H: Int = 640

        fun decompressImageRLE(array : ByteArray) : Bitmap {
            val width = GlobalStatus.IMG_W
            val height = GlobalStatus.IMG_H
            val image: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val colorArray = ArrayList<ArrayList<Int>>()

            for (w in 0 until width * height)
                colorArray.add(ArrayList())

            var x = 0
            var y = 0
            for (i in array.indices step 2) {
                for (j in 0 until array[i]) {
                    if (array[i + 1].toInt() < 0)
                        colorArray[y * width + x].add(array[i + 1].toInt() + 256)
                    else
                        colorArray[y * width + x].add(array[i + 1].toInt())
                    x += 1
                    if(x == width) {
                        x = 0
                        y += 1
                        if (y == height) {
                            y = 0
                        }
                    }
                }
            }

            for (l in colorArray.indices) {
                val xC = l % width
                val yC = l / width
                val red = colorArray[l][0]
                val green = colorArray[l][1]
                val blue = colorArray[l][2]
                image.setPixel(xC, yC, Color.rgb(red, green, blue))
            }
            return image
        }
    }
}