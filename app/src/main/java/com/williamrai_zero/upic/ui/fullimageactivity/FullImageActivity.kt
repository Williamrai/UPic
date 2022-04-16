package com.williamrai_zero.upic.ui.fullimageactivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.williamrai_zero.upic.R
import com.williamrai_zero.upic.databinding.ActivityFullImageBinding
import com.williamrai_zero.upic.util.ImageStorage
import com.williamrai_zero.upic.util.getImageName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

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

        try {
            binding.apply {
                Glide
                    .with(this@FullImageActivity)
                    .load(url)
                    .placeholder(circularProgressDrawable)
                    .into(ivFullImage)
            }
        } catch (e: Exception) {
            Toast.makeText(this,"${e.message}",Toast.LENGTH_LONG).show()
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
                    .setTitle(getString(R.string.permission_required))
                    .setMessage(getString(R.string.request))
                    .setPositiveButton(getString(R.string.give_permission)) { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    .setNegativeButton(getString(R.string.deny)) { dialog, _ ->
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
                        getString(R.string.image_saved_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}