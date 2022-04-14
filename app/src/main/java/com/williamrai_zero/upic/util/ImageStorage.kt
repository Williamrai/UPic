package com.williamrai_zero.upic.util


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import java.io.IOException


class ImageStorage(private val context: Context) {
    /**
     * saves the image to external storage
     * displayName: name for the image
     * bmp: the bitmap of image to save
     * returns@Boolean
     * MediaStore: Huge DB for all kinds of media files and corresponding metadata
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

        // saves image in MediaStore with imageCollection and metadata
        // ContentResolver to save the image
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