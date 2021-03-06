package com.williamrai_zero.upic.ui.mainactivity


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.*
import com.williamrai_zero.upic.MyApplication
import com.williamrai_zero.upic.R
import com.williamrai_zero.upic.model.ImageItem
import com.williamrai_zero.upic.repository.ImageRepository
import com.williamrai_zero.upic.util.networkutil.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ImageViewModel
@Inject
constructor(
    private val repository: ImageRepository,
    private val app: Application
) : AndroidViewModel(app) {

    private val _response = MutableLiveData<Resource<List<ImageItem>>>()
    private val context = getApplication<MyApplication>()

    val responseImage: LiveData<Resource<List<ImageItem>>>
        get() = _response

    init {
        getAllImages()
    }

    fun getAllImages() = viewModelScope.launch {
        safelyLoadImages()
    }


    private suspend fun safelyLoadImages() {
        _response.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                repository.getAllImages().let { response ->
                    _response.postValue(handleImageResponse(response))
                }
            } else {
                _response.postValue(Resource.Error( context.getString(R.string.network_msg_no_internet_connection)))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> _response.postValue(Resource.Error(context.getString(R.string.network_msg_network_failure)))
                else -> _response.postValue(Resource.Error("Error: ${t.message}"))
            }
        }
    }

    /**
     * decides to emit success or error state in the responseImage livedata
     * @param {Response<T>} response the response from the network
     * @return {Response<T>} Returns Resource state
     */
    private fun handleImageResponse(response: Response<List<ImageItem>>): Resource<List<ImageItem>> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    /**
     * checks the capabilities of the current network
     * @return {Boolean} Returns a boolean based on the status of the network
     */
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
            connectivityManager.activeNetworkInfo?. run {
                return when (type) {
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