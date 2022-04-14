package com.williamrai_zero.upic.util

import android.os.Build

/**
 * this function will make sure the device is running on sdk 29 and above
 */
inline fun <T> sdk29AbdUp(onSdk29: () -> T): T? {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdk29()
    } else null
}

fun getImageName(url: String): String {
    return url.split("/")[5]
}