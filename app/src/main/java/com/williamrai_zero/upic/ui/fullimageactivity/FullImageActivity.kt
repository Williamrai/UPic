package com.williamrai_zero.upic.ui.fullimageactivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.williamrai_zero.upic.databinding.ActivityFullImageBinding
import com.williamrai_zero.upic.util.ImageStorage
import com.williamrai_zero.upic.util.getImageName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FullImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullImageBinding
    private lateinit var imageStorage: ImageStorage
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
                onUpdateOrRequestStoragePermission()
            }
        }
    }

    /**
     * @param {String} url location of the image
     */
    private fun loadImageIntoView(url: String?) {
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        binding.apply {
            Glide
                .with(this@FullImageActivity)
                .load(url)
                .placeholder(circularProgressDrawable)
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
                onUpdateOrRequestStoragePermission()
            }

        }

    /**
     * the function enables permission request for API level below 29 and handles deny request permission
     * for above API level 29, we don't need write permission
     */
    private fun onUpdateOrRequestStoragePermission() {
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
                    .setPositiveButton("Give Permission") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    .setNegativeButton("Deny") { dialog, _ ->
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
        Log.d("saveImage", getImageName(url!!))
        CoroutineScope(Dispatchers.IO).launch {
            val isSaved = imageStorage.saveImageToExternalStorage(
                getImageName(url!!),
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