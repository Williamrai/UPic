package com.williamrai_zero.upic.repository

import com.williamrai_zero.upic.source.remote.ImageService
import javax.inject.Inject


class ImageRepository
@Inject constructor(private val api: ImageService) {

    suspend fun getAllImages() = api.getAllImage()

}