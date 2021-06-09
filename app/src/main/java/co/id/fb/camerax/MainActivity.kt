package co.id.fb.camerax

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.id.fb.camerax.databinding.ActivityMainBinding
import co.id.fb.camerax.utils.CameraActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    //private var lensFacing = CameraX.LensFacing.BACK
    //private var texture: TextureView = findViewById(R.id.texture)
    //private var btnTakePicture: MaterialButton = findViewById(R.id.btn_take_picture)

    /*
    private var metrics = DisplayMetrics()
    private var screenSize = Size(metrics.widthPixels, metrics.widthPixels)
    private var screenAspectRatio = Rational(0, 0)
    */

    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null
    //private var preview: Preview? = null

    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        */

        outputDirectory = getOutPutDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestPermissions()
    }

    private fun getOutPutDirectory(): File {
       val mediaDir = externalMediaDirs.firstOrNull()?.let {
           File(it, resources.getString(R.string.app_name)).apply {
               mkdirs()
           }
       }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = createPreviewUseCase()

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(Constans.TAG, "startCamera fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))

        /*preview.setOnPreviewOutputUpdateListener {
            val parent = texture.parent as ViewGroup
            parent.removeView(texture)
            parent.addView(texture, 0)

            texture.setSurfaceTexture(it.surfaceTexture)
            //texture.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Build the image capture use case and attach button click listener
        val imageCapture = createCaptureUseCase()
        */
        binding.btnTakePicture.setOnClickListener {
            Intent(this@MainActivity, CameraActivity::class.java).also { takeCameraIntent ->
                //typePhotoFile = 1
                takeCameraIntent.putExtra("surveyorID", "1000").putExtra("typePhoto", "1")
                startActivityForResult(takeCameraIntent, Constans.REQUEST_CODE_PERMISSION)
            }

            //takePicture()

            /*val file = File(
                //Environment.getExternalStorageDirectory(Environment.DIRECTORY_DCIM).toString() + "${MainActivity.folderPath}${System.currentTimeMillis()}.jpg"
                Environment.getExternalStorageDirectory(Environment.DIRECTORY_DCIM)
            )*/

            /*imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        error: ImageCapture.UseCaseError,
                        message: String, exc: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture successfully: ${file.absolutePath}"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                })
             */

        }

        //CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun createPreviewUseCase(): Preview {
        /*val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(windowManager?.defaultDisplay?.rotation ?: 0)
            setTargetRotation(texture.display.rotation)
        }.build()
        return Preview(previewConfig)*/


        return Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(
                    binding.viewFinder.surfaceProvider
                )
            }
    }

    //private fun createCaptureUseCase(): ImageCapture {
        /*
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(texture.display.rotation)
                setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                setFlashMode(FlashMode.AUTO)
            }

        return ImageCapture(imageCaptureConfig.build())
         */
    //}

    private fun requestPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
            Toast.makeText(this, "we have permission", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, Constans.REQUIRED_PERMISSION, Constans.REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constans.REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "permission not grant by the user", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() =
        Constans.REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

/*
    private fun toggleFrontBackCamera() {
        lensFacing = if (lensFacing == CameraX.LensFacing.BACK) {
            CameraX.LensFacing.FRONT
        } else {
            CameraX.LensFacing.BACK
        }
        texture.post { startCamera() }
    }
*/
    /*private fun setClickListeners() {
        toggleCameraLens.setOnClickListener { toggleFrontBackCamera() }
    }*/
/*
    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = texture.width / 2f
        val centerY = texture.height / 2f

        val rotationDegrees = when (texture.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        texture.setTransform(matrix)
    }
*/
    private fun takePicture() {
        val imageCapture = imageCapture?:return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constans.FILE_NAME_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val saveUri = Uri.fromFile(photoFile)
                    val msg = "photo saved"

                    Toast.makeText(this@MainActivity, "$msg $saveUri", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constans.TAG, "msgError: ${exception.message}", exception)
                }

            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}