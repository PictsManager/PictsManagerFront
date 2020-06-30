package com.pictsmanager.util

import android.graphics.Bitmap
import android.graphics.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class HuffmanNode(var value: Int, var freq: Int, var code: String?, var left: HuffmanNode?, var right: HuffmanNode?) {}

class MyComparator: Comparator<HuffmanNode> {
    override fun compare(o1: HuffmanNode, o2: HuffmanNode): Int {
        return o1.freq.minus(o2.freq)
    }
}

class Huffman {
    companion object {
        var redKey = Array(257) {_ -> "null"}
        var greenKey = Array(257) {_ -> "null"}
        var blueKey = Array(257) {_ -> "null"}

        var width = 0
        var height = 0

        fun buildFrequencies(toCompress: ArrayList<Int>): Map<Int, Int> {
            val target1: MutableMap<Int, Int> = mutableMapOf()

            for (i in toCompress) {
                if (target1.containsKey(i)) {
                    target1[i] = target1[i]?.plus(1) as Int
                } else {
                    target1[i] = 1
                }
            }

            /* sorted map by value */
            val target2: Map<Int, Int> = target1.toList()
                .sortedBy { (key, value) -> value }
                .toMap()

            return target2
        }

        private fun computeCode(root: HuffmanNode?, s: String, arr: Array<String?>, colorArray: ArrayList<Int>) {
            if (root!!.left == null && root.right == null) {
                root.code = s
                for (i in colorArray.indices) {
                    if (colorArray[i] == root.value) {
                        arr[i] = s
                    }
                }
                return
            }
            computeCode(root.left, s + "0", arr, colorArray)
            computeCode(root.right, s + "1", arr, colorArray)
        }

        private fun toDecimal(binaryNumber : String): Int {
            var sum = 0
            val deux = 2.0
            binaryNumber.reversed().forEachIndexed {
                    k, v -> sum += v.toString().toInt() * deux.pow(k).toInt()
            }
            return sum
        }

        private fun toBinary(decimalNumber: Int, binaryString: String = ""): String {
            while (decimalNumber > 0) {
                val temp = "${binaryString}${decimalNumber%2}"
                return toBinary(decimalNumber/2, temp)
            }
            return binaryString.reversed()
        }

        private fun encodeFromHuffmanNodeRoot(colorArray: ArrayList<Int>, root: HuffmanNode?): ByteArray {
            val arr = arrayOfNulls<String>(colorArray.size)
            computeCode(root, "", arr, colorArray)

            val encodedString = arr.joinToString("")
            val parseString = encodedString.chunked(8)

            var target = byteArrayOf()
            for (i in parseString) {
                val a = toDecimal(i)
                val b = a.toByte()
                target += b
            }
            return target
        }

        private fun flatToBinary(byteArray: ByteArray): String {
            val arrString = arrayListOf<String>()

            var j = 0

            for (i in byteArray) {
                var x = i.toInt()
                if (x < 0) {
                    x += 256
                }
                var a = Integer.toBinaryString(x)
                val la = a.length
                a = "0".repeat(8 - la) + a
                arrString.add(a)
            }
            return arrString.joinToString("")
        }

        private fun decode(binaryString: String, key: Array<String>): ArrayList<Int> {
            var s = ""
            val target = arrayListOf<Int>()

            for (i in binaryString) {
                s += i
                if (key.contains(s)) {
                    target.add(key.indexOf(s))
                    s = ""
                }
            }
            return target
        }

        fun buildHuffmanRootNode(freq: Map<Int, Int>): HuffmanNode? {
            val n = freq.size
            val q: PriorityQueue<HuffmanNode> = PriorityQueue<HuffmanNode>(n, MyComparator())
            var root: HuffmanNode? = null

            for ((k, v) in freq) {
                val hf = HuffmanNode(k, v, null, null, null)
                q.add(hf)
            }

            while (q.size > 1) {
                val x: HuffmanNode = q.peek()
                q.poll()
                val y: HuffmanNode = q.peek()
                q.poll()

                val f = HuffmanNode(-1, x.freq + y.freq, null, x, y)
                root = f

                q.add(f)
            }

            return root
        }

        private fun saveRedKey(root: HuffmanNode?) {
            if (root!!.left == null && root.right == null) {
                redKey[root.value] = root.code.toString()
                return
            }
            saveRedKey(root.left)
            saveRedKey(root.right)
        }

        private fun saveGreenKey(root: HuffmanNode?) {
            if (root!!.left == null && root.right == null) {
                greenKey[root.value] = root.code.toString()
                return
            }
            saveGreenKey(root.left)
            saveGreenKey(root.right)
        }

        private fun saveBlueKey(root: HuffmanNode?) {
            if (root!!.left == null && root.right == null) {
                blueKey[root.value] = root.code.toString()
                return
            }
            saveBlueKey(root.left)
            saveBlueKey(root.right)
        }

        fun applyCompress(bitmap: Bitmap): ByteArray {
            val redArray = ArrayList<Int>()
            val greenArray = ArrayList<Int>()
            val blueArray = ArrayList<Int>()

            width = bitmap.width
            height = bitmap.height

            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    redArray.add(Color.red(bitmap.getPixel(x, y)))
                    greenArray.add(Color.green(bitmap.getPixel(x, y)))
                    blueArray.add(Color.blue(bitmap.getPixel(x, y)))
                }
            }

            /*println("ten first red array encode")
            for (i in 0 until 10) {
                println(redArray[i])
            }*/

            val redFreq = buildFrequencies(redArray)
            val greenFreq = buildFrequencies(greenArray)
            val blueFreq = buildFrequencies(blueArray)

            val redRoot = buildHuffmanRootNode(redFreq)
            val greenRoot = buildHuffmanRootNode(greenFreq)
            val blueRoot = buildHuffmanRootNode(blueFreq)

            var redTarget = encodeFromHuffmanNodeRoot(redArray, redRoot)
            val greenTarget = encodeFromHuffmanNodeRoot(greenArray, greenRoot)
            val blueTarget = encodeFromHuffmanNodeRoot(blueArray, blueRoot)

            saveRedKey(redRoot)
            saveGreenKey(greenRoot)
            saveBlueKey(blueRoot)

            redKey[256] = 's' + redTarget.size.toString()
            greenKey[256] = 's' + greenTarget.size.toString()
            blueKey[256] = 's' + blueTarget.size.toString()

            redTarget = redTarget.plus(greenTarget)
            redTarget = redTarget.plus(blueTarget)

            return redTarget
        }

        fun applyDecompress(byteArray: ByteArray, width: Int, height: Int, keys: Array<Array<String>>): Bitmap {
            val image: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val rk = keys[0]
            val gk = keys[1]
            val bk = keys[2]

            //val rk = redKey
            //val gk = greenKey
            //val bk = blueKey

            val redSize = rk[256]!!.removeRange(0, 1).toInt()
            val greenSize = gk[256]!!.removeRange(0, 1).toInt()
            val blueSize = bk[256]!!.removeRange(0, 1).toInt()

            val redPart = byteArray.sliceArray(IntRange(0, redSize - 1))
            val greenPart = byteArray.sliceArray(IntRange(redSize, redSize + greenSize - 1))
            val bluePart = byteArray.sliceArray(IntRange(redSize + greenSize, redSize + greenSize + blueSize - 1))

            val res = flatToBinary(redPart)
            val ges = flatToBinary(greenPart)
            val bes = flatToBinary(bluePart)

            val redArray = decode(res, rk)
            val greenArray = decode(ges, gk)
            val blueArray = decode(bes, bk)

            /*println("ten first red array decode")
            for (i in 0 until 10) {
                println(redArray[i])
            }*/

            for (j in 0 until height) {
                for (i in 0 until width) {
                    val x = j * width + i
                    val r = redArray[x]
                    val g = greenArray[x]
                    val b = blueArray[x]
                    image.setPixel(i, j, Color.rgb(r, g, b))
                }
            }

            return image
        }
    }
}