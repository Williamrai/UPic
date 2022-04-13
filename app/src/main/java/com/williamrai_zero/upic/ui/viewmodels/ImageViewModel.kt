package com.williamrai_zero.upic.ui.viewmodels


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.williamrai_zero.upic.MyApplication
import com.williamrai_zero.upic.model.ImageItem
import com.williamrai_zero.upic.repository.ImageRepository
import com.williamrai_zero.upic.util.Constants.IMAGE_NETWORK_ERROR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ImageViewModel
@Inject
constructor(private val repository: ImageRepository, app: Application) :
    AndroidViewModel(app) {

    private val _response = MutableLiveData<List<ImageItem>>()

    val responseImage: LiveData<List<ImageItem>>
        get() = _response

    init {
        getAllImages()
    }

    private fun getAllImages() = viewModelScope.launch {
        safeLoadingImages()
    }

    private suspend fun safeLoadingImages() {
        try {
            if(checkInternetConnection()) {
                repository.getAllImages().let { response ->
                    if (response.isSuccessful) {
                        _response.postValue(response.body())
                    } else {
                        Log.d(IMAGE_NETWORK_ERROR, "ERROR")
                    }
                }
            } else {
                _response.postValue(listOf(ImageItem("no internet connection","","")))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException ->  _response.postValue(listOf(ImageItem("Network Failure","","")))
            }
        }
    }

    /**
     *
     */
    private fun checkInternetConnection(): Boolean {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }



}