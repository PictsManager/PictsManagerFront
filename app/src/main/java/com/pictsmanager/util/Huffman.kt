package com.pictsmanager.util

import android.graphics.Bitmap
import android.graphics.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class HuffmanNode(
    var value: Int,
    var freq: Int,
    var code: String?,
    var left: HuffmanNode?,
    var right: HuffmanNode?
)

class MyComparator : Comparator<HuffmanNode> {
    override fun compare(o1: HuffmanNode, o2: HuffmanNode): Int {
        return o1.freq.minus(o2.freq)
    }
}

/**
 * This class gives a method for compress and decompress images with Huffman algorithm.
 *
 * Constructed using the Huffman tree, an encoding, according to the following principle,
 * the more frequent the value the more its encoding will be short.
 */
class Huffman {
    companion object {
        var redKey = Array(257) { _ -> "null" }
        var greenKey = Array(257) { _ -> "null" }
        var blueKey = Array(257) { _ -> "null" }

        var width = 0
        var height = 0

        /**
         * This method gives the frequency of appearance of each number in a list.
         * The result is given with a dictionary where the key is the number and the value
         * is its frequency of appearance.
         *
         * At the end, the dictionary is ordered from the highest frequency to the lowest.
         */
        fun buildFrequencies(toCompress: ArrayList<Int>): Map<Int, Int> {
            val target1: MutableMap<Int, Int> = mutableMapOf()

            for (i in toCompress) {
                if (target1.containsKey(i)) {
                    target1[i] = target1[i]?.plus(1) as Int
                } else {
                    target1[i] = 1
                }
            }

            val target2: Map<Int, Int> = target1.toList()
                .sortedBy { (key, value) -> value }
                .toMap()

            return target2
        }

        /**
         * This method encode a byte array with Huffman's tree.
         *
         * Running through each node, if there is no child node, find each value in the byte array
         * that correspond to the node value, then complete a new array with binary string of the node.
         */
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

        /**
         * This method converts a binary string to its decimal value.
         */
        private fun toDecimal(binaryNumber : String): Int {
            var sum = 0
            val deux = 2.0
            binaryNumber.reversed().forEachIndexed { k, v ->
                sum += v.toString().toInt() * deux.pow(k).toInt()
            }
            return sum
        }

        /**
         * This method encode a byte array with Huffman logic.
         *
         * First, each byte is replaced by its encoding (referenced by Huffman's tree) into a new array.
         * Then, the array is flatten into a single binary string.
         * Finally, the binary string binary is convert into byte array
         */
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

        /**
         * This method converts byte array to binary string.
         */
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

        /**
         * This method decode the binary string.
         *
         * Iterate through the binary string. When the part of the string is found in key references
         * array target store the reference.
         */
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

        /**
         * This method builds the Huffman's tree from the dictionary of frequency.
         */
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

        /**
         * This method compress a bitmap with Huffman's algorithm, the result is a byte array.
         *
         * - Split the bitmap into 3 array of integers for red, green and blue.
         * - Compute the frequency of appearance of each number in color array, then compute the
         *   Huffman's tree resulting
         * - Encode color array, with the Huffman tree
         * - save keys
         */
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

        /**
         * This method decompress a byte array with Huffman's algorithm, the result is a bitmap.
         *
         * Main logic.
         */
        fun applyDecompress(byteArray: ByteArray, width: Int, height: Int, keys: Array<Array<String>>): Bitmap {
            val image: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val rk = keys[0]
            val gk = keys[1]
            val bk = keys[2]

            val redSize = rk[256].removeRange(0, 1).toInt()
            val greenSize = gk[256].removeRange(0, 1).toInt()
            val blueSize = bk[256].removeRange(0, 1).toInt()

            val redPart = byteArray.sliceArray(IntRange(0, redSize - 1))
            val greenPart = byteArray.sliceArray(IntRange(redSize, redSize + greenSize - 1))
            val bluePart = byteArray.sliceArray(
                IntRange(
                    redSize + greenSize,
                    redSize + greenSize + blueSize - 1
                )
            )

            val res = flatToBinary(redPart)
            val ges = flatToBinary(greenPart)
            val bes = flatToBinary(bluePart)

            val redArray = decode(res, rk)
            val greenArray = decode(ges, gk)
            val blueArray = decode(bes, bk)

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