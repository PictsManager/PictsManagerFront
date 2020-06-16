package com.pictsmanager.util

import android.graphics.Bitmap
import android.graphics.Color

class RLE {
    companion object {
        fun compressImage(bitmap: Bitmap) : ByteArray {
            var redArray = byteArrayOf()
            var greenArray = byteArrayOf()
            var blueArray = byteArrayOf()

            val h = bitmap.height
            val w = bitmap.width

            var countr = 1
            var countg = 1
            var countb = 1

            var previousRed: Int? = null
            var previousGreen: Int? = null
            var previousBlue: Int? = null

            var red = 0
            var green = 0
            var blue = 0

            for (j in 0 until h) {
                for (i in 0 until w) {

                    if (previousRed == null || previousGreen == null || previousBlue == null) {
                        previousRed = Color.red(bitmap.getPixel(i, j))
                        previousGreen = Color.green(bitmap.getPixel(i, j))
                        previousBlue = Color.blue(bitmap.getPixel(i, j))

                        continue
                    }

                    red = Color.red(bitmap.getPixel(i, j))
                    green = Color.green(bitmap.getPixel(i, j))
                    blue = Color.blue(bitmap.getPixel(i, j))

                    if (red == previousRed && countr < 125) {
                        countr += 1
                    } else {
                        redArray += countr.toByte()
                        redArray += previousRed.toByte()
                        countr = 1
                    }

                    if (green == previousGreen && countg < 125) {
                        countg += 1
                    } else {
                        greenArray += countg.toByte()
                        greenArray += previousGreen.toByte()
                        countg = 1
                    }

                    if (blue == previousBlue && countb < 125) {
                        countb += 1
                    } else {
                        blueArray += countb.toByte()
                        blueArray += previousBlue.toByte()
                        countb = 1
                    }

                    previousRed = red
                    previousGreen = green
                    previousBlue = blue
                }
            }

            redArray += countr.toByte()
            redArray += red.toByte()
            greenArray += countg.toByte()
            greenArray += green.toByte()
            blueArray += countb.toByte()
            blueArray += blue.toByte()

            redArray = redArray.plus(greenArray)
            redArray = redArray.plus(blueArray)

            return redArray
        }

        fun decompressImageRLE(
            array: ByteArray,
            width: Int,
            height: Int
        ) : Bitmap {
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