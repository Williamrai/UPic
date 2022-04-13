package com.williamrai_zero.upic.ui.fullimageactivity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.williamrai_zero.upic.R
import com.williamrai_zero.upic.databinding.ActivityFullImageBinding
import com.williamrai_zero.upic.util.ImageStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FullImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullImageBinding
    private lateinit var imageStorage: ImageStorage
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        url = intent.getStringExtra("url").toString()
        loadImageIntoView(url)

        imageStorage = ImageStorage(this)

        binding.apply {
            btnSave.setOnClickListener {
                onUpdateRequestPermission()
            }
        }
    }

    /**
     *
     */
    private fun loadImageIntoView(url: String?) {
        binding.apply {
            Glide
                .with(this@FullImageActivity)
                .load(url)
                .into(ivFullImage)
        }
    }

    /**
     * permission launcher
     * has a callback to handle granted and not granted case
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveImage(url)
            } else {
                onUpdateRequestPermission()
            }

        }

    /**
     *
     */
    private fun onUpdateRequestPermission() {
        // check current sdk version
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        var hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || minSdk29

        when {
            // permission already granted
            hasWritePermission -> {
                saveImage(url)
            }

            // shows why permission is required
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                // dialog to show why permission is required to the user
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please allow us to access Storage to save Image")
                    .setPositiveButton("Give Permission") { dialog, which ->
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    .setNegativeButton("Deny") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

            // permission has not been asked yet
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

    }


    /**
     * saves the image to external storage
     */
    private fun saveImage(url: String?) {
        Log.d("saveImage", "isSaveImage")
        CoroutineScope(Dispatchers.IO).launch {
            val isSaved = imageStorage.saveImageToExternalStorage(
                "Test",
                Glide.with(this@FullImageActivity)
                    .asBitmap().load(url).submit().get()
            )

            withContext(Dispatchers.Main) {
                if (isSaved) {
                    Toast.makeText(
                        this@FullImageActivity,
                        "Image Saved Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}