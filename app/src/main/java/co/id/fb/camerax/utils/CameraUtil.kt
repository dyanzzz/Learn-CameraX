package co.id.fb.camerax.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.TypedValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object CameraUtil {
    private var capturedFileName: String = ""
    private var capturedFilePath: String = ""

    @Throws(IOException::class)
    fun createImageFileEntrySurvey(context: Context, surveyorID: String, status: String): File {
        // Create an image file name
        val cal = Calendar.getInstance()
        val timeStamp = cal.timeInMillis
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        val prefixFileName = "${status}-${surveyorID}-${timeStamp}"
        capturedFileName = "${prefixFileName}.jpg"


        return File.createTempFile(
            prefixFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            capturedFilePath = absolutePath
        }
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, surveyorCode: String): File {
        // Create an image file name
        val cal = Calendar.getInstance()
        val timeStamp = cal.timeInMillis
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        val prefixFileName = "${surveyorCode}-${timeStamp}"
        capturedFileName = "${prefixFileName}.jpg"

        return File.createTempFile(
            prefixFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            capturedFilePath = absolutePath
        }
    }

    fun resizeFileFromUri(imageUri: Uri?, context: Context, textToDraw: String): File {
        val myFile: File = FileUtils.getFile(context, imageUri)
        var bitmapImage: Bitmap = BitmapFactory.decodeFile(myFile.canonicalPath)
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 500, 500, false)
        resizeBitmap(bitmapImage, 500)

        val bytes = ByteArrayOutputStream()
//        drawTextOnBitmap(bitmapImage, context, textToDraw)
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        try {
            val sd: File = context.cacheDir
            val folder = File(sd, "")
            if (!folder.exists()) {
                if (folder.mkdir()) {
                    folder.mkdirs()
                }
            }

            val file = File(folder.path + File.separator + myFile.name)
            val result: Boolean
            result = file.createNewFile()
            if (result) {
                val fo = FileOutputStream(myFile)
                fo.write(bytes.toByteArray())
                fo.close()
            }
        } catch (ie: IOException) {
            ie.printStackTrace()
        }
        return myFile
    }

    /*
    private fun drawTextOnBitmap(bitmap: Bitmap, context: Context, textToDraw: String) {
        val currentDate = Date().formatToViewDateTimeDefaults()
        val textSize: Int = 12
        val textSizeInSp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat(), context.resources.displayMetrics)
        val canvas = Canvas(bitmap)
        val fillPaint = Paint()
        fillPaint.color = Color.BLACK
        fillPaint.textSize = textSizeInSp
        fillPaint.style = Paint.Style.FILL
        canvas.drawText(textToDraw, 10f, 50f, fillPaint)
        canvas.drawText(currentDate, 10f, 80f, fillPaint)

        val strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeMiter = 5f
        strokePaint.textSize = textSizeInSp
        strokePaint.color = Color.WHITE
        canvas.drawText(textToDraw, 10f, 50f, strokePaint)
        canvas.drawText(currentDate, 10f, 80f, strokePaint)
    }
    */

    private fun resizeBitmap(getBitmap: Bitmap, maxSize: Int): Bitmap? {
        var width = getBitmap.width
        var height = getBitmap.height
        val x: Double
        if (width >= height && width > maxSize) {
            x = width / height.toDouble()
            width = maxSize
            height = (maxSize / x).toInt()
        } else if (height >= width && height > maxSize) {
            x = height / width.toDouble()
            height = maxSize
            width = (maxSize / x).toInt()
        }
        return Bitmap.createScaledBitmap(getBitmap, width, height, false)
    }

    fun encodeImage(imageUri: Uri?, context: Context): String? {
        val myFile: File = FileUtils.getFile(context, imageUri)
        var bitmapImage: Bitmap = BitmapFactory.decodeFile(myFile.canonicalPath)
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 120, 120, false)

        val baos = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val b = baos.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.DEFAULT)
    }
}