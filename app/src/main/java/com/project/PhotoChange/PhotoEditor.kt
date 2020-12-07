package com.project.PhotoChange

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.zomato.photofilters.imageprocessors.Filter
import kotlinx.coroutines.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class PhotoEditor : AppCompatActivity(), RecyclerViewAdapter.OnItemClickListener {


    private lateinit var imageViewPhoto: ImageView
    private lateinit var recyclerViewFilterBar: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var buttonSave: Button

    private var imageUri: Uri? = null
    private var source: ImageDecoder.Source? = null
    private var filter: Filter? = null

    private lateinit var bitmap: Bitmap
    private lateinit var result: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)
        initialize()
        when (intent.getIntExtra("type", -1)) {
            CAMERA_ACTIVITY_CODE -> {
                callCameraActivity()
            }
            GALLERY_ACTIVITY_CODE -> {
                callGalleryActivity()
            }
            else -> finish()
        }
    }
    private fun initialize() {

        imageViewPhoto = findViewById(R.id.imageView_photo_preview)
        recyclerViewFilterBar = findViewById(R.id.recyclerView_filters)
        recyclerViewAdapter = RecyclerViewAdapter(this)
        recyclerViewFilterBar.adapter = recyclerViewAdapter
        buttonSave = findViewById(R.id.button_save)

        buttonSave.setOnClickListener {

            val dialog = LoadingFragment().apply {
                isCancelable = false
            }.show(this@PhotoEditor.supportFragmentManager, TAG_LOADING_DIALOG)
            Thread(
                Runnable {
                    if (source != null) {
                        if (filter == null) {
                            saveImage(
                                ImageDecoder.decodeBitmap(source!!),
                                this@PhotoEditor
                            )
                        } else {
                            saveImage(
                                filter!!.processFilter(
                                    ImageDecoder.decodeBitmap(source!!)
                                        .copy(Bitmap.Config.RGBA_F16, true)
                                ),
                                this@PhotoEditor
                            )
                        }

                    }
                    supportFragmentManager.beginTransaction()
                        .remove(supportFragmentManager.findFragmentByTag(TAG_LOADING_DIALOG)!!)
                        .commit()
                }).start()
        }
    }

    override fun onItemClick(filter: Filter?, itemView: View?) {
        try {
            this.filter = filter
            result = if (filter != null) {
                filter.processFilter(bitmap.copy(Bitmap.Config.RGBA_F16, true))
            } else
                bitmap
            imageViewPhoto.setImageBitmap(result)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getOrientation(selectedImage: Uri): Int {
        var orientation = 0
        val projection = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cursor: Cursor? =
            applicationContext.contentResolver.query(selectedImage,
                projection, null, null, null)
        if (cursor != null) {
            val orientationColumnIndex: Int =
                cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION)
            if (cursor.moveToFirst()) {
                orientation = if (cursor.isNull(orientationColumnIndex)) 0 else cursor.getInt(
                    orientationColumnIndex
                )
            }
            cursor.close()
        }
        return orientation
    }


    private fun callCameraActivity() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Новое фото")
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun callGalleryActivity() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    try {
                        imageUri?.let {
                            source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                            bitmap = ImageDecoder.decodeBitmap(source!!)
                            bitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                bitmap.width / 3,
                                bitmap.height / 3,
                                false
                            )

                            var orientation: Int = 0
                            orientation = getOrientation(imageUri!!)
                            val matrix = Matrix().apply { setRotate(orientation.toFloat()) }

                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                            imageViewPhoto.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                GALLERY_REQUEST -> {
                    try {
                        imageUri = data.data
                        source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                        val inputStream = contentResolver.openInputStream(data.data!!)
                        bitmap = BitmapFactory.decodeStream(inputStream)
                        bitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            bitmap.width / 3,
                            bitmap.height / 3,
                            false
                        )

                        var orientation: Int = 0
                        if(imageUri!=null)
                        orientation = getOrientation(imageUri!!)
                        val matrix = Matrix().apply { setRotate(orientation.toFloat()) }

                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                        imageViewPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } else {
            finish()
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context) {
        val folderName = PHOTO_GALLERY_FOLDER
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory =
                File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}