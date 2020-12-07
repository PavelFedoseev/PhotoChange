package com.project.PhotoChange

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    private lateinit var buttonCamera: Button
    private lateinit var buttonGallery: Button
    private lateinit var textviewAppName: TextView
    private lateinit var imageButtonHelp: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initListeners()

    }

    private fun initViews() {
        buttonCamera = findViewById(R.id.button_camera)
        buttonGallery = findViewById(R.id.button_gallery)
        textviewAppName = findViewById(R.id.textView_app_name)
        textviewAppName.typeface = resources.getFont(R.font.app_name_font)
        imageButtonHelp = findViewById(R.id.imageButton_help)
    }

    private fun galleryPermissionRequest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(LOG_PHOTO_EDITOR_TAG, LOG_CAMERA_PERMISSION_SUCCESS)
            addPhotoEditorActivity(GALLERY_ACTIVITY_CODE)
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                GALLERY_PERMISSION_CODE
            )
        }
    }

    private fun cameraPermissionRequest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA

            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(LOG_PHOTO_EDITOR_TAG, LOG_CAMERA_PERMISSION_SUCCESS)
            addPhotoEditorActivity(CAMERA_ACTIVITY_CODE)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun initListeners() {
        buttonCamera.setOnClickListener {
            cameraPermissionRequest()
        }
        buttonGallery.setOnClickListener {
            galleryPermissionRequest()
        }
        textviewAppName.setOnLongClickListener {
            textviewAppName.text = "А чо, всмысле?!"
            Toast.makeText(this, "А чо, всмысле?", Toast.LENGTH_LONG).show()
            buttonGallery.text = "А чо, всмысле?!"
            buttonCamera.text = "А чо, всмысле?!"
            return@setOnLongClickListener true
        }
        imageButtonHelp.setOnClickListener {
            TutorialDialog().show(supportFragmentManager, TAG_TUTORIAL_DIALOG)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addPhotoEditorActivity(CAMERA_ACTIVITY_CODE)
            } else {
                Toast.makeText(this, "Доступ к камере запрещён", Toast.LENGTH_LONG).show()
            }
            GALLERY_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addPhotoEditorActivity(GALLERY_ACTIVITY_CODE)
            } else {
                Toast.makeText(this, "Доступ к галереи запрещён", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addPhotoEditorActivity(type: Int) {
        val intent = Intent(this, PhotoEditor::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    override fun onDestroy() {
        if(supportFragmentManager.findFragmentByTag(TAG_TUTORIAL_DIALOG)!=null)
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag(
            TAG_TUTORIAL_DIALOG)!!)
        super.onDestroy()
    }
}