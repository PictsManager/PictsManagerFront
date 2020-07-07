package com.pictsmanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_home.*
import java.io.*
import java.util.*

class HomeActivity : AppCompatActivity() {
    private var btnCapture: Button? = null
    private var textureView: TextureView? = null

    companion object {
        private val ORIENTATION = SparseIntArray()
        private const val REQUEST_CAMERA_PERMISSION = 200

        init {
            ORIENTATION.append(Surface.ROTATION_0, 0)
            ORIENTATION.append(Surface.ROTATION_90, 90)
            ORIENTATION.append(Surface.ROTATION_180, 180)
            ORIENTATION.append(Surface.ROTATION_270, 270)
        }
    }

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private val imageReader: ImageReader? = null
    private var file: File? = null
    private val mFlashSupported = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    var stateCallBack: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            var cameraDevice = cameraDevice
            cameraDevice.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        textureView = findViewById<View>(R.id.textureView) as TextureView
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        btnCapture = findViewById<View>(R.id.btnCapture) as Button
        btnCapture!!.setOnClickListener { takePicture() }
        initButtons()
    }

    private fun initButtons() {
        btnGallery.setOnClickListener {
            val intent = Intent(this@HomeActivity, GalleryActivity::class.java)
            startActivity(intent)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(this@HomeActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        btnProfil.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun takePicture() {
        if (cameraDevice == null) return
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size>? =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(ImageFormat.JPEG)


            val reader = ImageReader.newInstance(
                GlobalStatus.WIDTH,
                GlobalStatus.HEIGHT,
                ImageFormat.JPEG,
                1
            )
            val outputSurface: MutableList<Surface> = ArrayList(2)
            outputSurface.add(reader.surface)
            outputSurface.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            // Check orientation base on device
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0)

            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val readerListener: OnImageAvailableListener = object : OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer[bytes]
                        save(bytes)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var outputStream: OutputStream? = null
                    try {
                        var bmp: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        Log.d("IMAGE SIZE ::", bmp.height.toString())

                        val rotateImg = rotateImage(bmp, 90)

                        var filePath: String = tempFileImage(this@HomeActivity, rotateImg, "name")

                        val intent = Intent(this@HomeActivity, PictureActivity::class.java)

                        intent.putExtra("PictureTaken", filePath)

                        startActivity(intent)
                    } finally {
                        outputStream?.close()
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(
                outputSurface,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        try {
                            cameraCaptureSession.capture(
                                captureBuilder.build(),
                                captureListener,
                                mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun tempFileImage(context: Context, bitmap: Bitmap, name: String): String {

        var outputDir: File = context.cacheDir
        var imageFile: File = File(outputDir, name + ".jpg")

        var os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageFile.absolutePath
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            // Camera will rotate and inverse value of height and width
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(this@HomeActivity, "Changed", Toast.LENGTH_SHORT).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        captureRequestBuilder!!.set(
            CaptureRequest.CONTROL_MODE,
            CaptureRequest.CONTROL_MODE_AUTO
        )
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(manager.cameraIdList[0])
            val map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Check realtime permission if run higher API 23
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(manager.cameraIdList[0], stateCallBack, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView!!.isAvailable) openCamera() else textureView!!.surfaceTextureListener =
            textureListener
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }
}