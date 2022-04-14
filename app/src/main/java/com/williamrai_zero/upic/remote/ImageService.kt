package com.williamrai_zero.upic.remote

import com.williamrai_zero.upic.model.ImageItem
import com.williamrai_zero.upic.util.Constants.IMAGE_ENDPOINT
import retrofit2.Response
import retrofit2.http.GET


interface ImageService {
    @GET(IMAGE_ENDPOINT)
    suspend fun getAllImage(): Response<List<ImageItem>>

}