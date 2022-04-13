package com.williamrai_zero.upic.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import java.io.IOException
import kotlin.math.min

class ImageStorage(private val context: Context) {
    var readPermissionGranted = false
    var writePermissionGranted = false
    lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>


    /**
     * check permissions
     * if we dont have permissions update the readPermissionGranted and writePermissionGranted
     */
     fun updateRequestPermission() {
        Log.d("First","getPermissionsRequests")

        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        // check current sdk version
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        // update the permissions booleans
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(!readPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionRequest.toTypedArray())
        }
    }


    /**
     * saves the image to external storage
     * displayName: name for the image
     * bmp: the bitmap of image to save
     * returns@Boolean
     * MediaStore: Huge DB for all kinds of media files and corresponding mediadata
     * from 29 and above we can only access the primary external volume in external storage
     */
    fun saveImageToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        val imageCollection = sdk29AbdUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // metadata
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }

        // save image in MediaStore with imageCollection and metadata
        // ContentResolver to save image
        return try {
            context.contentResolver.insert(imageCollection, contentValues)?.also {uri ->
                context.contentResolver.openOutputStream(uri).use { outputStream ->
                    if(!bmp.compress(Bitmap.CompressFormat.JPEG,95,outputStream)) {
                        throw  IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Fail to create MediaStore Entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }


}